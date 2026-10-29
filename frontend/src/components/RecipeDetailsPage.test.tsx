import { fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { MealPlanContext } from '../MealPlanContext'
import type { MealSuggestionsResponse } from '../api/mealSuggestions'
import RecipeDetailsPage from './RecipeDetailsPage'

const suggestions: MealSuggestionsResponse = {
  generationMode: 'sample', message: 'Sample ideas.', recipes: [{
    code: 'soup', name: 'Potato soup', description: 'A warming soup.', cuisine: 'irish', mealType: 'dinner',
    servings: 2, cookingTimeMinutes: 30, dietLabels: ['vegetarian'],
    ingredients: [{ code: 'potato', name: 'Potato', form: 'Fresh', quantity: 1.25, unit: 'kg' }],
    steps: [{ number: 1, instruction: 'Chop the potatoes.' }, { number: 2, instruction: 'Simmer until soft.' }],
  }],
}

function renderDetails(code: string, value: MealSuggestionsResponse | null = suggestions) {
  return render(<MealPlanContext.Provider value={{ suggestions: value, setSuggestions: () => undefined }}>
    <MemoryRouter initialEntries={[`/recipes/${code}`]}>
      <Routes><Route path="/recipes/:recipeCode" element={<RecipeDetailsPage />} /><Route path="/" element={<p>Meal ideas</p>} /></Routes>
    </MemoryRouter>
  </MealPlanContext.Provider>)
}

describe('recipe details', () => {
  it('shows recipe content and adjusts servings without fetching', async () => {
    const user = userEvent.setup()
    renderDetails('soup')
    expect(screen.getByRole('heading', { name: 'Potato soup' })).toBeInTheDocument()
    expect(screen.getByText('A warming soup.')).toBeInTheDocument()
    expect(screen.getByText('1.25 kg Potato, Fresh')).toBeInTheDocument()
    expect(screen.getByText('Chop the potatoes.')).toBeInTheDocument()
    fireEvent.change(screen.getByRole('spinbutton', { name: 'Servings' }), { target: { value: '4' } })
    const ingredients = within(screen.getByRole('region', { name: 'Ingredients' }))
    expect(ingredients.getByText('2.5 kg Potato, Fresh')).toBeInTheDocument()
    fireEvent.change(screen.getByRole('spinbutton', { name: 'Servings' }), { target: { value: '1' } })
    expect(ingredients.getByText('0.625 kg Potato, Fresh')).toBeInTheDocument()
    await user.click(screen.getByRole('link', { name: '← Back to meal ideas' }))
    expect(screen.getByText('Meal ideas')).toBeInTheDocument()
  })

  it('explains when the requested recipe is not in memory', () => {
    renderDetails('missing', null)
    expect(screen.getByRole('heading', { name: 'Recipe unavailable' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Back to meal ideas' })).toHaveAttribute('href', '/')
  })
})
