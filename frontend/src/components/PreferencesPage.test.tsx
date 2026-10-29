import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { OptionsResponse } from '../api/options'
import PreferencesPage from './PreferencesPage'
import { MealPlanProvider } from '../MealPlanProvider'

const fetchMock = vi.fn<typeof fetch>()

const options: OptionsResponse = {
  cuisines: [
    { code: 'any', label: 'Any cuisine' },
    { code: 'american', label: 'American' },
    { code: 'irish', label: 'Irish' },
    { code: 'vietnamese', label: 'Vietnamese' },
  ],
  mealTypes: [
    { code: 'breakfast', label: 'Breakfast' },
    { code: 'lunch', label: 'Lunch' },
    { code: 'dinner', label: 'Dinner' },
  ],
  diets: [
    { code: 'none', label: 'None' },
    { code: 'vegetarian', label: 'Vegetarian' },
    { code: 'vegan', label: 'Vegan' },
  ],
  servings: { min: 1, max: 8 },
  maxCookingTimeMinutes: [15, 30, 45, 60],
  ingredients: [
    { code: 'tomato-canned', name: 'Tomatoes', form: 'Canned' },
    { code: 'tomato-cherry', name: 'Cherry tomatoes', form: 'Fresh' },
    { code: 'rice-white', name: 'Rice', form: 'White' },
    { code: 'pepper-bell', name: 'Pepper', form: 'Bell' },
  ],
  retailers: [
    { code: 'dunnes', name: 'Dunnes Stores' },
    { code: 'tesco', name: 'Tesco Ireland' },
  ],
  generationMode: 'sample',
  defaults: {
    cuisine: 'any',
    mealType: 'dinner',
    servings: 2,
    maxCookingTimeMinutes: 30,
    diet: 'none',
    excludedIngredientCodes: [],
    retailerCode: null,
  },
}

function optionsResponse(value: unknown = options) {
  return new Response(JSON.stringify(value), { status: 200 })
}

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false, gcTime: 0 } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter><MealPlanProvider><PreferencesPage /></MealPlanProvider></MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

describe('preference options and form', () => {
  it('shows a loading state while options are being fetched', () => {
    fetchMock.mockReturnValue(new Promise(() => {}))
    renderPage()
    expect(screen.getByRole('status')).toHaveTextContent('Loading your options')
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/options', { signal: expect.any(AbortSignal) })
  })

  it('shows backend choices and applies the returned defaults and bounds', async () => {
    fetchMock.mockResolvedValue(optionsResponse())
    renderPage()

    const cuisine = await screen.findByRole('combobox', { name: 'Cuisine' })
    expect(cuisine).toHaveValue('any')
    expect(within(cuisine).getByRole('option', { name: 'American' })).toHaveValue('american')
    expect(within(cuisine).getByRole('option', { name: 'Irish' })).toHaveValue('irish')
    expect(within(cuisine).getByRole('option', { name: 'Vietnamese' })).toHaveValue('vietnamese')
    expect(screen.getByRole('combobox', { name: 'Meal type' })).toHaveValue('dinner')
    expect(screen.getByRole('spinbutton', { name: /Servings/ })).toHaveValue(2)
    expect(screen.getByRole('spinbutton', { name: /Servings/ })).toHaveAttribute('min', '1')
    expect(screen.getByRole('spinbutton', { name: /Servings/ })).toHaveAttribute('max', '8')
    expect(screen.getByRole('combobox', { name: 'Maximum cooking time' })).toHaveValue('30')
    expect(screen.getByRole('combobox', { name: 'Diet' })).toHaveValue('none')
  })

  it('lets visitors select cuisine, meal, cooking time, and diet options', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValue(optionsResponse())
    renderPage()

    await user.selectOptions(await screen.findByRole('combobox', { name: 'Cuisine' }), 'vietnamese')
    await user.selectOptions(screen.getByRole('combobox', { name: 'Meal type' }), 'lunch')
    await user.clear(screen.getByRole('spinbutton', { name: /Servings/ }))
    await user.type(screen.getByRole('spinbutton', { name: /Servings/ }), '8')
    await user.selectOptions(screen.getByRole('combobox', { name: 'Maximum cooking time' }), '45')
    await user.selectOptions(screen.getByRole('combobox', { name: 'Diet' }), 'vegan')

    expect(screen.getByRole('combobox', { name: 'Cuisine' })).toHaveValue('vietnamese')
    expect(screen.getByRole('combobox', { name: 'Meal type' })).toHaveValue('lunch')
    expect(screen.getByRole('spinbutton', { name: /Servings/ })).toHaveValue(8)
    expect(screen.getByRole('combobox', { name: 'Maximum cooking time' })).toHaveValue('45')
    expect(screen.getByRole('combobox', { name: 'Diet' })).toHaveValue('vegan')
  })

  it('searches ingredient name and form, adds selected codes once, and removes tags', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValue(optionsResponse())
    renderPage()
    const search = await screen.findByRole('searchbox', { name: 'Ingredients to exclude' })

    await user.type(search, 'cAnNeD')
    const matches = screen.getByRole('list', { name: 'Matching ingredients' })
    const addTomatoes = within(matches).getByRole('button', { name: 'Add Tomatoes (Canned) to exclusions' })
    expect(within(matches).queryByRole('button', { name: /Cherry tomatoes/ })).not.toBeInTheDocument()
    await user.click(addTomatoes)

    expect(search).toHaveValue('')
    expect(screen.getByRole('list', { name: 'Excluded ingredients' })).toHaveTextContent('Tomatoes')
    await user.type(search, 'tomato')
    expect(screen.getByRole('list', { name: 'Matching ingredients' })).toHaveTextContent('Cherry tomatoes')
    expect(screen.getByRole('list', { name: 'Matching ingredients' })).not.toHaveTextContent('Canned')
    await user.click(screen.getByRole('button', { name: 'Remove Tomatoes Canned from exclusions' }))
    expect(screen.queryByRole('list', { name: 'Excluded ingredients' })).not.toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Add Tomatoes (Canned) to exclusions' })).toBeInTheDocument()
  })

  it('allows clearing the search when there are no matching ingredients', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValue(optionsResponse())
    renderPage()
    const search = await screen.findByRole('searchbox', { name: 'Ingredients to exclude' })
    await user.type(search, 'not-in-catalog')
    expect(screen.getByRole('status')).toHaveTextContent('No matching ingredients')
  })

  it('offers a manual retry after options cannot be loaded', async () => {
    const user = userEvent.setup()
    fetchMock.mockRejectedValueOnce(new TypeError('offline')).mockResolvedValueOnce(optionsResponse())
    renderPage()

    expect(await screen.findByRole('alert')).toHaveTextContent('couldn’t load the available choices')
    await user.click(screen.getByRole('button', { name: 'Retry loading options' }))
    expect(await screen.findByRole('combobox', { name: 'Cuisine' })).toHaveValue('any')
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(2))
  })

  it('reports a malformed options response as a retryable loading error', async () => {
    fetchMock.mockResolvedValue(optionsResponse({ cuisines: [] }))
    renderPage()
    expect(await screen.findByRole('alert')).toHaveTextContent('couldn’t load the available choices')
  })

  it('submits preferences and renders sample suggestions', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValueOnce(optionsResponse()).mockResolvedValueOnce(new Response(JSON.stringify({
      generationMode: 'sample', message: 'Here are sample meal ideas.', recipes: [{
        code: 'irish-potato-soup', name: 'Irish potato soup', description: 'A warming soup.', cuisine: 'irish', mealType: 'dinner',
        servings: 2, cookingTimeMinutes: 45, dietLabels: ['vegetarian'], ingredients: [], steps: [],
      }],
    }), { status: 200 }))
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Find meal ideas' }))
    expect(await screen.findByText('Irish potato soup')).toBeInTheDocument()
    expect(screen.getByText('Sample recipe')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/meal-suggestions', expect.objectContaining({ method: 'POST' }))
    expect(JSON.parse(String(fetchMock.mock.calls[1][1]?.body))).toMatchObject({ cuisine: 'any', mealType: 'dinner', servings: 2 })
    expect(JSON.parse(String(fetchMock.mock.calls[1][1]?.body))).not.toHaveProperty('retailerCode')
  })

  it('keeps successful suggestions and form values after a later request fails', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValueOnce(optionsResponse()).mockResolvedValueOnce(new Response(JSON.stringify({
      generationMode: 'sample', message: 'Sample ideas.', recipes: [{ code: 'x', name: 'First idea', description: 'A meal.', cuisine: 'irish', mealType: 'dinner', servings: 2, cookingTimeMinutes: 20, dietLabels: [], ingredients: [], steps: [] }],
    }), { status: 200 })).mockRejectedValueOnce(new TypeError('offline'))
    renderPage()
    const cuisine = await screen.findByRole('combobox', { name: 'Cuisine' })
    await user.selectOptions(cuisine, 'irish')
    await user.click(screen.getByRole('button', { name: 'Find meal ideas' }))
    expect(await screen.findByText('First idea')).toBeInTheDocument()
    await user.selectOptions(cuisine, 'vietnamese')
    await user.click(screen.getByRole('button', { name: 'Find meal ideas' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('previous suggestions are still here')
    expect(cuisine).toHaveValue('vietnamese')
    expect(screen.getByText('First idea')).toBeInTheDocument()
    expect(screen.getByText(/earlier preferences/)).toBeInTheDocument()
  })

  it('shows loading and empty result states, then replaces them on regeneration', async () => {
    const user = userEvent.setup()
    let release: ((response: Response) => void) | undefined
    fetchMock.mockResolvedValueOnce(optionsResponse()).mockReturnValueOnce(new Promise((resolve) => { release = resolve }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ generationMode: 'sample', message: 'Fresh ideas.', recipes: [{ code: 'next', name: 'New idea', description: 'Another meal.', cuisine: 'american', mealType: 'dinner', servings: 2, cookingTimeMinutes: 20, dietLabels: [], ingredients: [], steps: [] }] }), { status: 200 }))
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Find meal ideas' }))
    expect(screen.getByRole('status')).toHaveTextContent('Finding sample meal ideas')
    release?.(new Response(JSON.stringify({ generationMode: 'sample', message: 'No sample meals match.', recipes: [] }), { status: 200 }))
    expect(await screen.findByText('No meals found. Try changing your preferences.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Find meal ideas' }))
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3))
    expect(await screen.findByText('New idea')).toBeInTheDocument()
  })
})
