import { Link } from 'react-router'
import { useMealPlan } from '../useMealPlan'
import type { GroceryStatus } from '../groceries'
import { formatQuantity } from '../recipeQuantities'

export default function GroceriesPage() {
  const { selectedRecipe, servings, groceries, setGroceries } = useMealPlan()
  const updateStatus = (key: string, status: GroceryStatus) => setGroceries((items) => items.map((item) => item.key === key ? { ...item, status: item.status === status ? 'needed' : status } : item))
  return <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
    <Link to={selectedRecipe ? `/recipes/${selectedRecipe.code}` : '/'} className="font-semibold text-emerald-800 underline">← {selectedRecipe ? 'Back to recipe' : 'Back to meal ideas'}</Link>
    <h2 className="mt-6 text-3xl font-semibold">Grocery checklist</h2>
    {selectedRecipe ? <p className="mt-2 text-slate-600">{selectedRecipe.name} · {servings ?? selectedRecipe.servings} servings</p> : <p className="mt-3 text-slate-600">Create a checklist from a recipe to see ingredients here.</p>}
    {groceries.length > 0 && <ul aria-label="Grocery items" className="mt-6 divide-y divide-slate-200">{groceries.map((item) => <li key={item.key} className="flex flex-wrap items-center justify-between gap-3 py-4">
      <span className={item.status !== 'needed' ? 'text-slate-500 line-through' : 'text-slate-800'}>{formatQuantity(item.quantity)} {item.unit} {item.name}{item.form ? `, ${item.form}` : ''}</span>
      <div className="flex gap-2"><button type="button" aria-pressed={item.status === 'owned'} onClick={() => updateStatus(item.key, 'owned')} className="rounded-lg border border-slate-300 px-3 py-2 text-sm">{item.status === 'owned' ? 'Owned ✓' : 'Owned'}</button><button type="button" aria-pressed={item.status === 'purchased'} onClick={() => updateStatus(item.key, 'purchased')} className="rounded-lg border border-slate-300 px-3 py-2 text-sm">{item.status === 'purchased' ? 'Purchased ✓' : 'Purchased'}</button></div>
    </li>)}</ul>}
  </section>
}
