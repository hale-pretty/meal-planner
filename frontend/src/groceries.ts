import type { MealIngredient } from './api/mealSuggestions'

export type GroceryStatus = 'needed' | 'owned' | 'purchased'
export type GroceryItem = MealIngredient & { status: GroceryStatus; key: string }

function unitInfo(unit: string): { dimension: string; unit: string; factor: number } {
  switch (unit.trim().toUpperCase()) {
    case 'G': case 'GRAM': case 'GRAMS': return { dimension: 'mass', unit: 'g', factor: 1 }
    case 'KG': case 'KILOGRAM': case 'KILOGRAMS': return { dimension: 'mass', unit: 'g', factor: 1000 }
    case 'ML': case 'MILLILITER': case 'MILLILITRE': return { dimension: 'volume', unit: 'ml', factor: 1 }
    case 'L': case 'LITER': case 'LITRE': return { dimension: 'volume', unit: 'ml', factor: 1000 }
    case 'EACH': case 'EA': case 'COUNT': return { dimension: 'count', unit: 'each', factor: 1 }
    default: return { dimension: `unit:${unit.trim().toLowerCase()}`, unit: unit.trim(), factor: 1 }
  }
}

export function buildGroceryItems(ingredients: MealIngredient[], servings: number, originalServings: number, previous: GroceryItem[] = []): GroceryItem[] {
  const result: GroceryItem[] = []
  const indexes = new Map<string, number>()
  for (const ingredient of ingredients) {
    const info = unitInfo(ingredient.unit)
    const identity = `${ingredient.code}\u0000${ingredient.form}\u0000${info.dimension}`
    const key = identity
    const scaled = Number((ingredient.quantity * servings / originalServings * info.factor).toFixed(3))
    const index = indexes.get(identity)
    if (index === undefined) {
      indexes.set(identity, result.length)
      const old = previous.find((item) => item.key === key)
      result.push({ ...ingredient, quantity: scaled, unit: info.unit, key, status: old?.status ?? 'needed' })
    } else {
      result[index].quantity = Number((result[index].quantity + scaled).toFixed(3))
    }
  }
  return result
}
