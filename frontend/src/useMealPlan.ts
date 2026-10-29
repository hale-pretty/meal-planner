import { useContext } from 'react'
import { MealPlanContext } from './MealPlanContext'

export function useMealPlan() {
  const value = useContext(MealPlanContext)
  if (!value) throw new Error('useMealPlan must be used within MealPlanProvider')
  return value
}
