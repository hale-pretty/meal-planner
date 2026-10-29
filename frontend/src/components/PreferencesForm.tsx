import { useState } from 'react'
import type { FormEvent } from 'react'
import type { OptionsResponse } from '../api/options'
import { fetchMealSuggestions, type MealPreferences } from '../api/mealSuggestions'
import { Link } from 'react-router'
import { useMealPlan } from '../useMealPlan'

type PreferencesFormProps = { options: OptionsResponse }

export default function PreferencesForm({ options }: PreferencesFormProps) {
  const { suggestions, setSuggestions } = useMealPlan()
  const [cuisine, setCuisine] = useState(options.defaults.cuisine)
  const [mealType, setMealType] = useState(options.defaults.mealType)
  const [servings, setServings] = useState(String(options.defaults.servings))
  const [cookingTime, setCookingTime] = useState(String(options.defaults.maxCookingTimeMinutes))
  const [diet, setDiet] = useState(options.defaults.diet)
  const [excludedCodes, setExcludedCodes] = useState(options.defaults.excludedIngredientCodes)
  const [ingredientSearch, setIngredientSearch] = useState('')
  const [suggestionsPreferences, setSuggestionsPreferences] = useState<MealPreferences | null>(null)
  const [loading, setLoading] = useState(false)
  const [requestError, setRequestError] = useState(false)

  const selectedIngredients = options.ingredients.filter((ingredient) => excludedCodes.includes(ingredient.code))
  const search = ingredientSearch.trim().toLocaleLowerCase()
  const matchingIngredients = search
    ? options.ingredients.filter((ingredient) =>
      !excludedCodes.includes(ingredient.code)
      && `${ingredient.name} ${ingredient.form}`.toLocaleLowerCase().includes(search))
    : []

  function removeIngredient(code: string) {
    setExcludedCodes((current) => current.filter((selected) => selected !== code))
  }

  function addIngredient(code: string) {
    setExcludedCodes((current) => current.includes(code) ? current : [...current, code])
    setIngredientSearch('')
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const preferences: MealPreferences = { cuisine, mealType, servings: Number(servings),
      maxCookingTimeMinutes: Number(cookingTime), diet, excludedIngredientCodes: excludedCodes }
    setLoading(true); setRequestError(false)
    try {
      const response = await fetchMealSuggestions(preferences)
      setSuggestions(response); setSuggestionsPreferences(preferences)
    } catch { setRequestError(true) } finally { setLoading(false) }
  }

  return (
    <section aria-labelledby="preferences-heading" className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <h2 id="preferences-heading" className="text-2xl font-semibold text-slate-950">Your meal preferences</h2>
      <p className="mt-2 leading-relaxed text-slate-600">Choose a few details to shape your meal ideas.</p>

      <form onSubmit={submit} className="mt-8 space-y-6">
        <div className="grid gap-6 sm:grid-cols-2">
          <label className="block font-medium text-slate-800">
            Cuisine
            <select value={cuisine} onChange={(event) => setCuisine(event.target.value)}
              className="mt-2 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800">
              {options.cuisines.map((choice) => <option key={choice.code} value={choice.code}>{choice.label}</option>)}
            </select>
          </label>

          <label className="block font-medium text-slate-800">
            Meal type
            <select value={mealType} onChange={(event) => setMealType(event.target.value)}
              className="mt-2 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800">
              {options.mealTypes.map((choice) => <option key={choice.code} value={choice.code}>{choice.label}</option>)}
            </select>
          </label>

          <label className="block font-medium text-slate-800">
            Servings
            <input type="number" min={options.servings.min} max={options.servings.max} step={1} inputMode="numeric"
              value={servings} onChange={(event) => setServings(event.target.value)}
              className="mt-2 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800" />
            <span className="mt-1 block text-sm font-normal text-slate-500">Choose {options.servings.min}–{options.servings.max} servings.</span>
          </label>

          <label className="block font-medium text-slate-800">
            Maximum cooking time
            <select value={cookingTime} onChange={(event) => setCookingTime(event.target.value)}
              className="mt-2 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800">
              {options.maxCookingTimeMinutes.map((minutes) => <option key={minutes} value={minutes}>{minutes} minutes</option>)}
            </select>
          </label>

          <label className="block font-medium text-slate-800">
            Diet
            <select value={diet} onChange={(event) => setDiet(event.target.value)}
              className="mt-2 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800">
              {options.diets.map((choice) => <option key={choice.code} value={choice.code}>{choice.label}</option>)}
            </select>
          </label>

        </div>

        <div>
          <label htmlFor="ingredient-search" className="block font-medium text-slate-800">Ingredients to exclude</label>
          <p id="ingredient-help" className="mt-1 text-sm leading-relaxed text-slate-500">
            Search by ingredient name or form, then choose it to add it to your exclusions.
          </p>
          <input id="ingredient-search" type="search" autoComplete="off" value={ingredientSearch}
            onChange={(event) => setIngredientSearch(event.target.value)} aria-describedby="ingredient-help"
            placeholder="For example, tomato or canned"
            className="mt-3 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 placeholder:text-slate-400 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800" />

          {search && (
            matchingIngredients.length > 0 ? (
              <ul aria-label="Matching ingredients" className="mt-2 max-h-56 overflow-y-auto rounded-lg border border-slate-200 bg-white p-1">
                {matchingIngredients.map((ingredient) => (
                  <li key={ingredient.code}>
                    <button type="button" onClick={() => addIngredient(ingredient.code)}
                      aria-label={`Add ${ingredient.name} (${ingredient.form}) to exclusions`}
                      className="w-full rounded-md px-3 py-2 text-left text-slate-800 hover:bg-emerald-50 focus-visible:outline-2 focus-visible:outline-emerald-800">
                      <span className="font-medium">{ingredient.name}</span>
                      <span className="ml-2 text-sm text-slate-500">{ingredient.form}</span>
                    </button>
                  </li>
                ))}
              </ul>
            ) : <p role="status" className="mt-2 text-sm text-slate-600">No matching ingredients.</p>
          )}

          {selectedIngredients.length > 0 && (
            <ul aria-label="Excluded ingredients" className="mt-4 flex flex-wrap gap-2">
              {selectedIngredients.map((ingredient) => (
                <li key={ingredient.code}>
                  <span className="inline-flex items-center gap-2 rounded-full bg-emerald-50 py-1 pl-3 pr-1 text-sm text-emerald-950">
                    {ingredient.name} <span className="text-emerald-800">{ingredient.form}</span>
                    <button type="button" onClick={() => removeIngredient(ingredient.code)}
                      aria-label={`Remove ${ingredient.name} ${ingredient.form} from exclusions`}
                      className="rounded-full p-1 font-bold hover:bg-emerald-100 focus-visible:outline-2 focus-visible:outline-emerald-800">×</button>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
        <button type="submit" disabled={loading}
          className="rounded-lg bg-emerald-800 px-5 py-3 font-semibold text-white hover:bg-emerald-900 disabled:cursor-wait disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-emerald-800">
          {loading ? 'Finding meal ideas…' : 'Find meal ideas'}
        </button>
        {loading && <p role="status" className="text-slate-600">Finding sample meal ideas…</p>}
        {requestError && <p role="alert" className="text-red-800">We couldn’t load meal ideas. Your preferences{suggestions ? ' and previous suggestions are still here' : ' are unchanged'}; please try again.</p>}
        {suggestions && <section aria-labelledby="suggestions-heading" className="border-t border-slate-200 pt-6">
          <h3 id="suggestions-heading" className="text-xl font-semibold text-slate-950">Sample meal ideas</h3>
          {suggestionsPreferences && JSON.stringify({cuisine,mealType,servings:Number(servings),maxCookingTimeMinutes:Number(cookingTime),diet,excludedIngredientCodes:excludedCodes}) !== JSON.stringify(suggestionsPreferences)
            && <p className="mt-2 text-sm text-amber-800">These suggestions reflect your earlier preferences. Find meal ideas again to refresh them.</p>}
          <p className="mt-2 text-slate-600">{suggestions.message}</p>
          {suggestions.recipes.length === 0 ? <p role="status" className="mt-4 rounded-lg bg-slate-50 p-4 text-slate-700">No meals found. Try changing your preferences.</p>
            : <ul className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{suggestions.recipes.map((recipe) => <li key={recipe.code} className="rounded-xl border border-slate-200 p-5">
              <span className="text-xs font-semibold uppercase tracking-wide text-emerald-800">Sample recipe</span>
              <h4 className="mt-2 text-lg font-semibold text-slate-950">{recipe.name}</h4>
              <p className="mt-2 text-sm text-slate-600">{recipe.description}</p>
              <p className="mt-3 text-sm text-slate-700">{recipe.cuisine} · {recipe.mealType} · {recipe.cookingTimeMinutes} min · {recipe.servings} servings</p>
              {recipe.dietLabels.length > 0 && <p className="mt-1 text-sm text-slate-600">{recipe.dietLabels.join(', ')}</p>}
              <Link to={`/recipes/${encodeURIComponent(recipe.code)}`} className="mt-4 inline-block font-semibold text-emerald-800 underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800">View recipe: {recipe.name}</Link>
            </li>)}</ul>}
        </section>}
      </form>
    </section>
  )
}
