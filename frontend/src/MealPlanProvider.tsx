import { useState } from 'react'
import type { ReactNode } from 'react'
import { MealPlanContext } from './MealPlanContext'
import type { MealRecipe, MealSuggestionsResponse } from './api/mealSuggestions'

export function MealPlanProvider({ children }: { children: ReactNode }) {
  const [suggestions, setSuggestions] = useState<MealSuggestionsResponse | null>(null)
  const [selectedRecipe, setSelectedRecipe] = useState<MealRecipe | null>(null)
  const [servings, setServings] = useState<number | null>(null)
  const [groceries, setGroceries] = useState<import('./groceries').GroceryItem[]>([])
  return <MealPlanContext.Provider value={{ suggestions, setSuggestions, selectedRecipe, setSelectedRecipe, servings, setServings, groceries, setGroceries }}>{children}</MealPlanContext.Provider>
}
