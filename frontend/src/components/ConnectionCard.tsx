import { useEffect, useState } from 'react'

type ConnectionState = 'loading' | 'connected' | 'error'

const failureMessage = 'The backend could not be reached or returned an unexpected response. Check that it is running and try again.'

export default function ConnectionCard() {
  const [state, setState] = useState<ConnectionState>('loading')
  const [error, setError] = useState(failureMessage)
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => {
      controller.abort()
      setError('The backend did not respond within five seconds. Check that it is running and try again.')
      setState('error')
    }, 5_000)

    async function checkConnection() {
      try {
        const response = await fetch('/api/v1/health', { signal: controller.signal })
        if (!response.ok) throw new Error('Health request failed')
        const data: unknown = await response.json()
        if (typeof data !== 'object' || data === null || !('status' in data) || data.status !== 'UP') {
          throw new Error('Unexpected health response')
        }
        if (!controller.signal.aborted) setState('connected')
      } catch {
        if (!controller.signal.aborted) {
          setError(failureMessage)
          setState('error')
        }
      } finally {
        window.clearTimeout(timeout)
      }
    }

    void checkConnection()
    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [attempt])

  const statusText = state === 'loading'
    ? 'Checking backend connection…'
    : state === 'connected' ? 'Backend connected' : 'Backend connection unavailable'
  const dotColor = state === 'connected' ? 'bg-emerald-600' : state === 'error' ? 'bg-amber-600' : 'bg-slate-400'

  function retry() {
    setState('loading')
    setAttempt((value) => value + 1)
  }

  return (
    <section aria-labelledby="connection-heading" className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <h2 id="connection-heading" className="text-sm font-semibold uppercase tracking-wider text-slate-500">Connection status</h2>
      <div role="status" aria-live="polite" aria-atomic="true" className="mt-4">
        <p className="flex items-center gap-3 text-xl font-semibold text-slate-950">
          <span aria-hidden="true" className={`h-3 w-3 shrink-0 rounded-full ${dotColor}`} />
          {statusText}
        </p>
        <p className="mt-3 max-w-xl leading-relaxed text-slate-600">
          {state === 'error' ? error : state === 'connected'
            ? 'Everything is ready for the next step.'
            : 'This should only take a moment.'}
        </p>
      </div>
      <button type="button" onClick={retry} disabled={state === 'loading'}
        className="mt-6 rounded-lg bg-emerald-800 px-5 py-3 font-semibold text-white hover:bg-emerald-900 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-emerald-800 disabled:cursor-wait disabled:opacity-50">
        Retry connection
      </button>
    </section>
  )
}
