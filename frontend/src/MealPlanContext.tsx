import { createContext } from 'react'
import type { Dispatch, SetStateAction } from 'react'
import type { MealSuggestionsResponse } from './api/mealSuggestions'
import type { MealRecipe } from './api/mealSuggestions'
import type { GroceryItem } from './groceries'

export type MealPlanContextValue = {
  suggestions: MealSuggestionsResponse | null
  setSuggestions: Dispatch<SetStateAction<MealSuggestionsResponse | null>>
  selectedRecipe: MealRecipe | null
  setSelectedRecipe: Dispatch<SetStateAction<MealRecipe | null>>
  servings: number | null
  setServings: Dispatch<SetStateAction<number | null>>
  groceries: GroceryItem[]
  setGroceries: Dispatch<SetStateAction<GroceryItem[]>>
}

export const MealPlanContext = createContext<MealPlanContextValue | null>(null)

