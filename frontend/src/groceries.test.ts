import { describe, expect, it } from 'vitest'
import { buildGroceryItems } from './groceries'
import type { MealIngredient } from './api/mealSuggestions'

const ingredients: MealIngredient[] = [
  { code: 'flour', name: 'Flour', form: '', quantity: 500, unit: 'g' },
  { code: 'flour', name: 'Flour', form: '', quantity: 0.5, unit: 'kg' },
  { code: 'milk', name: 'Milk', form: '', quantity: 250, unit: 'ml' },
  { code: 'milk', name: 'Milk', form: '', quantity: 0.25, unit: 'l' },
  { code: 'egg', name: 'Egg', form: '', quantity: 2, unit: 'EACH' },
  { code: 'milk', name: 'Milk', form: '', quantity: 1, unit: 'g' },
]

describe('buildGroceryItems', () => {
  it('combines compatible quantities into canonical units and preserves first-seen order', () => {
    const result = buildGroceryItems(ingredients, 2, 2)
    expect(result.map(({ code, unit, quantity }) => [code, unit, quantity])).toEqual([
      ['flour', 'g', 1000], ['milk', 'ml', 500], ['egg', 'each', 2], ['milk', 'g', 1],
    ])
  })

  it('scales for servings and keeps statuses for matching entries', () => {
    const prior = buildGroceryItems(ingredients, 2, 2)
    prior[0].status = 'purchased'
    const result = buildGroceryItems(ingredients, 4, 2, prior)
    expect(result[0].quantity).toBe(2000)
    expect(result[0].status).toBe('purchased')
    expect(result[1].quantity).toBe(1000)
  })
})
