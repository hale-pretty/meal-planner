export type Choice = { code: string; label: string }
export type IngredientOption = { code: string; name: string; form: string }
export type RetailerOption = { code: string; name: string }

export type OptionsResponse = {
  cuisines: Choice[]
  mealTypes: Choice[]
  diets: Choice[]
  servings: { min: number; max: number }
  maxCookingTimeMinutes: number[]
  ingredients: IngredientOption[]
  retailers: RetailerOption[]
  generationMode: string
  defaults: {
    cuisine: string
    mealType: string
    servings: number
    maxCookingTimeMinutes: number
    diet: string
    excludedIngredientCodes: string[]
    retailerCode: string | null
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isChoice(value: unknown): value is Choice {
  return isRecord(value) && typeof value.code === 'string' && typeof value.label === 'string'
}

function isIngredient(value: unknown): value is IngredientOption {
  return isRecord(value) && typeof value.code === 'string'
    && typeof value.name === 'string' && typeof value.form === 'string'
}

function isRetailer(value: unknown): value is RetailerOption {
  return isRecord(value) && typeof value.code === 'string' && typeof value.name === 'string'
}

export function isOptionsResponse(value: unknown): value is OptionsResponse {
  if (!isRecord(value) || !isRecord(value.servings) || !isRecord(value.defaults)) return false
  const defaults = value.defaults
  return Array.isArray(value.cuisines) && value.cuisines.every(isChoice)
    && Array.isArray(value.mealTypes) && value.mealTypes.every(isChoice)
    && Array.isArray(value.diets) && value.diets.every(isChoice)
    && typeof value.servings.min === 'number' && typeof value.servings.max === 'number'
    && Array.isArray(value.maxCookingTimeMinutes)
    && value.maxCookingTimeMinutes.every((minutes) => typeof minutes === 'number')
    && Array.isArray(value.ingredients) && value.ingredients.every(isIngredient)
    && Array.isArray(value.retailers) && value.retailers.every(isRetailer)
    && typeof value.generationMode === 'string'
    && typeof defaults.cuisine === 'string' && typeof defaults.mealType === 'string'
    && typeof defaults.servings === 'number' && typeof defaults.maxCookingTimeMinutes === 'number'
    && typeof defaults.diet === 'string' && Array.isArray(defaults.excludedIngredientCodes)
    && defaults.excludedIngredientCodes.every((code) => typeof code === 'string')
    && (defaults.retailerCode === null || typeof defaults.retailerCode === 'string')
}

export async function fetchOptions(signal?: AbortSignal): Promise<OptionsResponse> {
  const response = await fetch('/api/v1/options', { signal })
  if (!response.ok) throw new Error('Options request failed')

  const data: unknown = await response.json()
  if (!isOptionsResponse(data)) throw new Error('Unexpected options response')
  return data
}
