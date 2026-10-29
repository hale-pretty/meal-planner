import { createContext } from 'react'
import type { Dispatch, SetStateAction } from 'react'
import type { MealSuggestionsResponse } from './api/mealSuggestions'

type MealPlanContextValue = {
  suggestions: MealSuggestionsResponse | null
  setSuggestions: Dispatch<SetStateAction<MealSuggestionsResponse | null>>
}

export const MealPlanContext = createContext<MealPlanContextValue | null>(null)

