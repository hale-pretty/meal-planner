import { fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { Link, MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { MealPlanContext } from '../MealPlanContext'
import type { MealPlanContextValue } from '../MealPlanContext'
import type { MealSuggestionsResponse } from '../api/mealSuggestions'
import type { GroceryItem } from '../groceries'
import RecipeDetailsPage from './RecipeDetailsPage'
import GroceriesPage from './GroceriesPage'

const suggestions: MealSuggestionsResponse = {
  generationMode: 'sample', message: 'Sample ideas.', recipes: [{
    code: 'soup', name: 'Potato soup', description: 'A warming soup.', cuisine: 'irish', mealType: 'dinner',
    servings: 2, cookingTimeMinutes: 30, dietLabels: [],
    ingredients: [{ code: 'potato', name: 'Potato', form: 'Fresh', quantity: 1, unit: 'kg' }], steps: [],
  }, {
    code: 'stew', name: 'Bean stew', description: 'A hearty stew.', cuisine: 'irish', mealType: 'dinner',
    servings: 2, cookingTimeMinutes: 25, dietLabels: [],
    ingredients: [{ code: 'bean', name: 'Beans', form: 'Dried', quantity: 200, unit: 'g' }], steps: [],
  }],
}

function Harness() {
  const [selectedRecipe, setSelectedRecipe] = useState<MealPlanContextValue['selectedRecipe']>(null)
  const [servings, setServings] = useState<number | null>(null)
  const [groceries, setGroceries] = useState<GroceryItem[]>([])
  const value: MealPlanContextValue = { suggestions, setSuggestions: () => undefined, selectedRecipe, setSelectedRecipe, servings, setServings, groceries, setGroceries }
  return <MealPlanContext.Provider value={value}><MemoryRouter initialEntries={['/recipes/soup']}><Link to="/recipes/stew">Switch recipe</Link><Routes>
    <Route path="/recipes/:recipeCode" element={<RecipeDetailsPage />} /><Route path="/groceries" element={<GroceriesPage />} />
  </Routes></MemoryRouter></MealPlanContext.Provider>
}

describe('grocery checklist flow', () => {
  it('creates a checklist, recalculates servings, and toggles owned status', async () => {
    const user = userEvent.setup()
    render(<Harness />)
    await user.click(screen.getByRole('button', { name: 'Create grocery checklist' }))
    expect(screen.getByRole('heading', { name: 'Grocery checklist' })).toBeInTheDocument()
    expect(screen.getByText('1000 g Potato, Fresh')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Owned' }))
    expect(screen.getByRole('button', { name: 'Owned ✓' })).toHaveAttribute('aria-pressed', 'true')
    await user.click(screen.getByRole('link', { name: '← Back to recipe' }))
    fireEvent.change(screen.getByRole('spinbutton', { name: 'Servings' }), { target: { value: '4' } })
    await user.click(screen.getByRole('button', { name: 'Create grocery checklist' }))
    expect(screen.getByText('2000 g Potato, Fresh')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Owned ✓' })).toHaveAttribute('aria-pressed', 'true')
  })

  it('keeps the existing checklist on cancel and replaces it after confirmation', async () => {
    const user = userEvent.setup()
    render(<Harness />)
    await user.click(screen.getByRole('button', { name: 'Create grocery checklist' }))
    await user.click(screen.getByRole('button', { name: 'Owned' }))
    await user.click(screen.getByRole('link', { name: '← Back to recipe' }))
    await user.click(screen.getByRole('link', { name: 'Switch recipe' }))
    await user.click(screen.getByRole('button', { name: 'Create grocery checklist' }))
    const dialog = screen.getByRole('dialog', { name: 'Replace grocery checklist?' })
    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Create grocery checklist' }))
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Replace checklist' }))
    expect(screen.getByText(/Bean stew/)).toBeInTheDocument()
    expect(screen.getByText('200 g Beans, Dried')).toBeInTheDocument()
  })
})
