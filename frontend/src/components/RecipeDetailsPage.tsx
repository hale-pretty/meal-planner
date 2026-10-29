import { Link, useParams } from 'react-router'
import { useState } from 'react'
import { useMealPlan } from '../useMealPlan'
import { formatQuantity, scaleIngredients } from '../recipeQuantities'

export default function RecipeDetailsPage() {
  const { recipeCode } = useParams()
  const { suggestions } = useMealPlan()
  const recipe = suggestions?.recipes.find((item) => item.code === recipeCode)
  const [servingsByRecipe, setServingsByRecipe] = useState<Record<string, string>>({})

  if (!recipe) return <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
    <h2 className="text-2xl font-semibold">Recipe unavailable</h2>
    <p className="mt-3 text-slate-600">This recipe isn’t in your current meal ideas. Find meal ideas to choose a recipe.</p>
    <Link to="/" className="mt-5 inline-block font-semibold text-emerald-800 underline">Back to meal ideas</Link>
  </section>

  const servingsInput = servingsByRecipe[recipe.code] ?? String(recipe.servings)
  const parsedServings = Number(servingsInput)
  const currentServings = Number.isInteger(parsedServings) && parsedServings >= 1 && parsedServings <= 8
    ? parsedServings : recipe.servings
  const ingredients = scaleIngredients(recipe.ingredients, recipe.servings, currentServings)
  return <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
    <Link to="/" className="font-semibold text-emerald-800 underline">← Back to meal ideas</Link>
    <p className="mt-6 text-sm font-semibold uppercase tracking-wide text-emerald-800">{recipe.cuisine}</p>
    <h2 className="mt-2 text-3xl font-semibold text-slate-950">{recipe.name}</h2>
    <p className="mt-3 leading-relaxed text-slate-600">{recipe.description}</p>
    <p className="mt-4 text-sm text-slate-700">{recipe.cookingTimeMinutes} minutes · {recipe.mealType}{recipe.dietLabels.length ? ` · ${recipe.dietLabels.join(', ')}` : ''}</p>
    <label className="mt-6 inline-flex items-center gap-3 font-medium text-slate-800">Servings
      <input aria-label="Servings" type="number" min="1" max="8" step="1" value={currentServings}
        onChange={(event) => setServingsByRecipe((current) => ({ ...current, [recipe.code]: event.target.value }))}
        className="w-20 rounded-lg border border-slate-300 px-3 py-2" />
    </label>
    <section className="mt-8" aria-labelledby="ingredients-heading">
      <h3 id="ingredients-heading" className="text-xl font-semibold">Ingredients</h3>
      <ul className="mt-3 list-disc space-y-2 pl-6 text-slate-700">{ingredients.map((item) => <li key={item.code}>{formatQuantity(item.quantity)} {item.unit} {item.name}{item.form ? `, ${item.form}` : ''}</li>)}</ul>
    </section>
    <section className="mt-8" aria-labelledby="steps-heading">
      <h3 id="steps-heading" className="text-xl font-semibold">Instructions</h3>
      <ol className="mt-3 list-decimal space-y-3 pl-6 leading-relaxed text-slate-700">{recipe.steps.map((step) => <li key={step.number}>{step.instruction}</li>)}</ol>
    </section>
  </article>
}
