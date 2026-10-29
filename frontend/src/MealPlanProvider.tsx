import { useState } from 'react'
import type { ReactNode } from 'react'
import { MealPlanContext } from './MealPlanContext'
import type { MealSuggestionsResponse } from './api/mealSuggestions'

export function MealPlanProvider({ children }: { children: ReactNode }) {
  const [suggestions, setSuggestions] = useState<MealSuggestionsResponse | null>(null)
  return <MealPlanContext.Provider value={{ suggestions, setSuggestions }}>{children}</MealPlanContext.Provider>
}
