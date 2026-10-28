export type MealIngredient = { code: string; name: string; form: string; quantity: number; unit: string }
export type MealRecipe = {
  code: string; name: string; description: string; cuisine: string; mealType: string
  servings: number; cookingTimeMinutes: number; dietLabels: string[]
  ingredients: MealIngredient[]; steps: { number: number; instruction: string }[]
}
export type MealSuggestionsResponse = { generationMode: 'sample'; message: string; recipes: MealRecipe[] }
export type MealPreferences = {
  cuisine: string; mealType: string; servings: number; maxCookingTimeMinutes: number
  diet: string; excludedIngredientCodes: string[]
}

export async function fetchMealSuggestions(preferences: MealPreferences): Promise<MealSuggestionsResponse> {
  const response = await fetch('/api/v1/meal-suggestions', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(preferences),
  })
  if (!response.ok) throw new Error('Suggestions request failed')
  const data: unknown = await response.json()
  if (typeof data !== 'object' || data === null || !('generationMode' in data)
    || data.generationMode !== 'sample' || !('message' in data) || typeof data.message !== 'string'
    || !('recipes' in data) || !Array.isArray(data.recipes)) throw new Error('Unexpected suggestions response')
  return data as MealSuggestionsResponse
}
