import { describe, expect, it } from 'vitest'
import { formatQuantity, scaleQuantity } from './recipeQuantities'

describe('recipe quantity scaling', () => {
  it('scales up and down from the recipe serving baseline', () => {
    expect(scaleQuantity(1.5, 2, 4)).toBe(3)
    expect(scaleQuantity(1.5, 2, 1)).toBe(0.75)
  })

  it('rounds to three decimal places and removes trailing zeroes', () => {
    expect(formatQuantity(scaleQuantity(0.3333, 3, 5))).toBe('0.555')
    expect(formatQuantity(scaleQuantity(1, 2, 4))).toBe('2')
  })

  it('calculates each adjustment from the unchanged original quantity', () => {
    const originalQuantity = 0.5
    const doubled = scaleQuantity(originalQuantity, 2, 4)
    expect(scaleQuantity(originalQuantity, 2, 3)).toBe(0.75)
    expect(scaleQuantity(originalQuantity, 2, 1)).toBe(0.25)
    expect(doubled).toBe(1)
  })
})
