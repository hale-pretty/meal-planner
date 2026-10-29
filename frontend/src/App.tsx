import { BrowserRouter, Link, Route, Routes, useLocation } from 'react-router'
import { MealPlanProvider } from './MealPlanProvider'
import PreferencesPage from './components/PreferencesPage'
import RecipeDetailsPage from './components/RecipeDetailsPage'

export default function App() {
  return (
    <BrowserRouter>
      <MealPlanProvider>
        <AppLayout />
      </MealPlanProvider>
    </BrowserRouter>
  )
}

function AppLayout() {
  const location = useLocation()
  const isRecipe = location.pathname.startsWith('/recipes/')
  return <main className="mx-auto flex min-h-screen max-w-4xl flex-col justify-center px-6 py-16 sm:px-12">
    <header className="mb-10 max-w-2xl">
      <p className="mb-4 text-sm font-semibold uppercase tracking-widest text-emerald-800">Meal Planner</p>
      <h1 className="text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">A little planning.<br />A better mealtime.</h1>
      <p className="mt-6 leading-relaxed text-slate-600">Choose what works for you, and we’ll use it to find meal ideas.</p>
    </header>
    <div hidden={isRecipe}>
      <PreferencesPage />
    </div>
    <Routes>
      <Route path="/" element={null} />
      <Route path="/recipes/:recipeCode" element={<RecipeDetailsPage />} />
      <Route path="*" element={<p>Page not found. <Link to="/">Return to meal ideas</Link>.</p>} />
    </Routes>
  </main>
}
