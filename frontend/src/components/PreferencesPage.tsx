import { useQuery } from '@tanstack/react-query'
import { fetchOptions } from '../api/options'
import PreferencesForm from './PreferencesForm'

export default function PreferencesPage() {
  const options = useQuery({
    queryKey: ['options'],
    queryFn: ({ signal }) => fetchOptions(signal),
    retry: false,
    staleTime: 5 * 60 * 1000,
  })

  if (options.isPending) {
    return (
      <section aria-labelledby="preferences-heading" className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <h2 id="preferences-heading" className="text-2xl font-semibold text-slate-950">Your meal preferences</h2>
        <p role="status" className="mt-4 text-slate-600">Loading your options…</p>
      </section>
    )
  }

  if (options.isError) {
    return (
      <section aria-labelledby="preferences-heading" className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <h2 id="preferences-heading" className="text-2xl font-semibold text-slate-950">Your meal preferences</h2>
        <p role="alert" className="mt-4 text-slate-600">
          We couldn’t load the available choices. Check that the backend is running, then try again.
        </p>
        <button type="button" onClick={() => void options.refetch()} disabled={options.isFetching}
          className="mt-6 rounded-lg bg-emerald-800 px-5 py-3 font-semibold text-white hover:bg-emerald-900 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-emerald-800">
          {options.isFetching ? 'Retrying…' : 'Retry loading options'}
        </button>
      </section>
    )
  }

  return <PreferencesForm options={options.data} />
}
