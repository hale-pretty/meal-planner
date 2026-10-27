import PreferencesPage from './components/PreferencesPage'

export default function App() {
  return (
    <main className="mx-auto flex min-h-screen max-w-4xl flex-col justify-center px-6 py-16 sm:px-12">
      <header className="mb-10 max-w-2xl">
        <p className="mb-4 text-sm font-semibold uppercase tracking-widest text-emerald-800">Meal Planner</p>
        <h1 className="text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">A little planning.<br />A better mealtime.</h1>
        <p className="mt-6 text-lg leading-relaxed text-slate-600">
          Choose what works for you, and we’ll use it to find meal ideas.
        </p>
      </header>
      <PreferencesPage />
    </main>
  )
}
