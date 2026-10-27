import { useState } from 'react'
import type { FormEvent } from 'react'
import type { OptionsResponse } from '../api/options'

type PreferencesFormProps = { options: OptionsResponse }

export default function PreferencesForm({ options }: PreferencesFormProps) {
  const [cuisine, setCuisine] = useState(options.defaults.cuisine)
  const [mealType, setMealType] = useState(options.defaults.mealType)
  const [servings, setServings] = useState(String(options.defaults.servings))
  const [cookingTime, setCookingTime] = useState(String(options.defaults.maxCookingTimeMinutes))
  const [diet, setDiet] = useState(options.defaults.diet)
  const [retailerCode, setRetailerCode] = useState(options.defaults.retailerCode ?? '')
  const [excludedCodes, setExcludedCodes] = useState(options.defaults.excludedIngredientCodes)
  const [ingredientSearch, setIngredientSearch] = useState('')

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

  function preventSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
  }

  return (
    <section aria-labelledby="preferences-heading" className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <h2 id="preferences-heading" className="text-2xl font-semibold text-slate-950">Your meal preferences</h2>
      <p className="mt-2 leading-relaxed text-slate-600">Choose a few details to shape your meal ideas.</p>

      <form onSubmit={preventSubmit} className="mt-8 space-y-6">
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

          <label className="block font-medium text-slate-800">
            Preferred retailer
            <select value={retailerCode} onChange={(event) => setRetailerCode(event.target.value)}
              className="mt-2 block w-full rounded-lg border border-slate-300 bg-white px-3 py-3 text-slate-950 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-800">
              <option value="">All supported retailers</option>
              {options.retailers.map((retailer) => <option key={retailer.code} value={retailer.code}>{retailer.name}</option>)}
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
      </form>
    </section>
  )
}
