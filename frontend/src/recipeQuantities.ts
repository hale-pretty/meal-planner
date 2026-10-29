import type { MealIngredient } from './api/mealSuggestions'

export function scaleQuantity(quantity: number, originalServings: number, servings: number): number {
  return Number((quantity * servings / originalServings).toFixed(3))
}

export function formatQuantity(quantity: number): string {
  return String(Number(quantity.toFixed(3)))
}

export function scaleIngredients(ingredients: MealIngredient[], originalServings: number, servings: number) {
  return ingredients.map((ingredient) => ({ ...ingredient, quantity: scaleQuantity(ingredient.quantity, originalServings, servings) }))
}
