import { StrictMode } from 'react'
import { act, fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ConnectionCard from './ConnectionCard'

const fetchMock = vi.fn<typeof fetch>()
const healthyResponse = () => new Response(JSON.stringify({ status: 'UP' }))
const retryButton = () => screen.getByRole('button', { name: 'Retry connection' })

beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

describe('backend connection', () => {
  it('starts loading and disables retry while checking', () => {
    fetchMock.mockReturnValue(new Promise(() => {}))
    render(<ConnectionCard />)
    expect(screen.getByRole('status')).toHaveTextContent('Checking backend connection…')
    expect(retryButton()).toBeDisabled()
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/health', { signal: expect.any(AbortSignal) })
  })

  it('announces a valid health response and enables retry', async () => {
    fetchMock.mockResolvedValue(healthyResponse())
    render(<ConnectionCard />)
    expect(await screen.findByText('Backend connected')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Backend connected')
    expect(retryButton()).toBeEnabled()
  })

  it.each([
    ['network failure', () => Promise.reject(new TypeError('Failed to fetch'))],
    ['HTTP failure even with an UP body', () => Promise.resolve(new Response('{"status":"UP"}', { status: 503 }))],
    ['malformed JSON', () => Promise.resolve(new Response('not json'))],
    ['missing status', () => Promise.resolve(new Response('{}'))],
    ['unexpected status', () => Promise.resolve(new Response('{"status":"DOWN"}'))],
    ['null', () => Promise.resolve(new Response('null'))],
    ['array', () => Promise.resolve(new Response('[]'))],
    ['primitive', () => Promise.resolve(new Response('"UP"'))],
  ])('explains %s', async (_name, response) => {
    fetchMock.mockImplementation(response)
    render(<ConnectionCard />)
    expect(await screen.findByText('Backend connection unavailable')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('could not be reached or returned an unexpected response')
    expect(retryButton()).toBeEnabled()
  })

  it('can retry successfully after a network failure', async () => {
    const user = userEvent.setup()
    fetchMock.mockRejectedValueOnce(new TypeError('offline')).mockResolvedValueOnce(healthyResponse())
    render(<ConnectionCard />)
    await screen.findByText('Backend connection unavailable')
    await user.click(retryButton())
    expect(await screen.findByText('Backend connected')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })

  it('times out at five seconds, aborts the request, and retries successfully', async () => {
    vi.useFakeTimers()
    fetchMock.mockReturnValueOnce(new Promise(() => {})).mockResolvedValueOnce(healthyResponse())
    render(<ConnectionCard />)
    const signal = fetchMock.mock.calls[0]![1]!.signal!
    await act(() => vi.advanceTimersByTimeAsync(4_999))
    expect(screen.getByRole('status')).toHaveTextContent('Checking backend connection…')
    await act(() => vi.advanceTimersByTimeAsync(1))
    expect(signal.aborted).toBe(true)
    expect(screen.getByRole('status')).toHaveTextContent('did not respond within five seconds')
    expect(retryButton()).toBeEnabled()
    fireEvent.click(retryButton())
    expect(retryButton()).toBeDisabled()
    await act(() => vi.advanceTimersByTimeAsync(0))
    expect(screen.getByRole('status')).toHaveTextContent('Backend connected')
  })

  it('keeps the timeout active while reading a stalled response body', async () => {
    vi.useFakeTimers()
    fetchMock.mockResolvedValue(new Response(new ReadableStream()))
    render(<ConnectionCard />)
    await act(() => vi.advanceTimersByTimeAsync(5_000))
    expect(screen.getByRole('status')).toHaveTextContent('did not respond within five seconds')
    expect(fetchMock.mock.calls[0]![1]!.signal!.aborted).toBe(true)
  })

  it('ignores a late response from a timed out request after retry', async () => {
    vi.useFakeTimers()
    let resolveFirst!: (value: Response) => void
    fetchMock.mockReturnValueOnce(new Promise((resolve) => { resolveFirst = resolve }))
      .mockRejectedValueOnce(new TypeError('still offline'))
    render(<ConnectionCard />)
    await act(() => vi.advanceTimersByTimeAsync(5_000))
    fireEvent.click(retryButton())
    await act(() => vi.advanceTimersByTimeAsync(0))
    await act(async () => { resolveFirst(healthyResponse()) })
    expect(screen.getByRole('status')).toHaveTextContent('Backend connection unavailable')
  })

  it('cancels pending work and removes the timer on unmount', () => {
    vi.useFakeTimers()
    fetchMock.mockReturnValue(new Promise(() => {}))
    const { unmount } = render(<ConnectionCard />)
    const signal = fetchMock.mock.calls[0]![1]!.signal!
    unmount()
    expect(signal.aborted).toBe(true)
    expect(vi.getTimerCount()).toBe(0)
  })

  it('handles StrictMode effect cleanup without losing the current request', async () => {
    fetchMock.mockReturnValueOnce(new Promise(() => {})).mockResolvedValueOnce(healthyResponse())
    render(<StrictMode><ConnectionCard /></StrictMode>)
    expect(await screen.findByText('Backend connected')).toBeInTheDocument()
    expect(fetchMock.mock.calls[0]![1]!.signal!.aborted).toBe(true)
    expect(fetchMock.mock.calls[1]![1]!.signal!.aborted).toBe(false)
  })
})
