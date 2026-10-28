# Frontend — T05

React and TypeScript frontend for Meal Planner. It loads supported choices, submits preferences for sample meal suggestions, and displays summary cards. Suggestions and preferences are not saved across reloads.

## Prerequisites and Setup

Install Node.js 22.12 or later, including npm. Check your terminal:

```sh
node --version
npm --version
```

If you use nvm, run `nvm install` and `nvm use` inside `frontend/` to select Node 22 from `.nvmrc`. The project enforces its minimum Node version through `package.json` and `.npmrc`.

From the repository root, install the locked dependencies:

```sh
cd frontend
npm ci
```

Use npm and keep `package-lock.json` in version control. Dependency installation requires access to the npm registry. The setup was verified with Node.js 22.23.4 and npm 10.9.9; Node was temporarily installed outside the repository for the agent's checks, so it still needs to be available in your own terminal.

## Run Both Applications Locally

First start Docker and PostgreSQL using the [backend database setup](../backend/README.md#start-postgresql), including the private root `.env` file. Then use two terminals, starting each from the repository root.

**Terminal 1 — backend:**

```sh
cd backend
./gradlew bootRun
```

The backend needs JDK 21 and runs at `http://localhost:8080`. See the [backend guide](../backend/README.md) for Java setup and endpoint checks.

**Terminal 2 — frontend:**

```sh
cd frontend
npm run dev
```

Open **http://localhost:5173** in your browser. Keep both terminals running while checking the form. Vite requires port 5173 to be free and exits with an error if it is occupied. Stop a server with Ctrl+C in its terminal.

PostgreSQL is required from T03 onward. No AI key is needed. Keep database passwords in the backend environment; never add secrets to frontend files or `VITE_` variables.

## Check the Preferences Form in Your Browser

1. With both applications running, open or refresh `http://localhost:5173`. The page loads supported preferences from `GET /api/v1/options`, then shows the form with backend-provided defaults.
2. Select American, Irish, Vietnamese, and Any cuisine; try the meal, serving, and cooking-time controls. Retailer selection belongs after choosing a dish, when its shopping list is prepared.
3. Search exclusions by ingredient name or form. Choose a result to add it as a removable tag. A selected ingredient no longer appears in the results until removed.
4. Stop Spring Boot with Ctrl+C in terminal 1. Refresh the page; expect a useful options-loading error and **Retry loading options** button. Restart Spring Boot and retry.
5. Select **Find meal ideas** to request up to three sample cards. Try exclusions and diets that return no matches. If a request fails, the form and last successful suggestions remain; changing preferences marks those suggestions as based on earlier preferences.
6. Resize the browser or use its mobile device toolbar. Use Tab to reach each input, result, tag removal button, and action; focus should be visible.

Suggestions use only the seeded sample catalog. No AI provider is called and choices are not saved across reloads.

## Automated Checks

Run these from `frontend/`:

| Command | Purpose |
| --- | --- |
| `npm run dev` | Start the Vite development server |
| `npm test` | Run component tests once and exit |
| `npm run test:watch` | Watch files and rerun tests interactively; press `q` to exit |
| `npm run build` | Check strict TypeScript types and create production assets |
| `npm run lint` | Run Oxlint |

Component tests use Vitest, React Testing Library, jest-dom, and jsdom. They mock `fetch`, so neither server needs to be running. They cover options loading and errors, retry, malformed response handling, backend-provided defaults and bounds, preference selection, ingredient exclusions, suggestions submission and cards, loading/empty/error states, preserved results, and regeneration.

A production build verifies that assets compile. Production API hosting is part of T16; the development proxy is not packaged into `dist/`.

## How the Pieces Fit Together

- `src/main.tsx` mounts React with StrictMode and the shared TanStack Query client.
- `src/App.tsx` renders the page header and preferences feature.
- `src/api/options.ts` fetches `/api/v1/options`; `src/api/mealSuggestions.ts` submits preferences and checks the sample response envelope.
- `src/components/PreferencesPage.tsx` uses TanStack Query to load and cache options, show loading/error states, and let visitors retry.
- `src/components/PreferencesForm.tsx` owns controlled form values, filters the downloaded ingredient catalog by name/form, submits suggestions, and retains the previous successful response after a request failure.
- `src/index.css` imports Tailwind. `vite.config.ts` registers its Vite plugin, following the [official Tailwind setup](https://tailwindcss.com/docs/installation/using-vite).
- `vite.config.ts` also sets port 5173, the API proxy, and the jsdom test environment. `src/test/setup.ts` installs DOM assertions and cleans up after tests.

The development request flow is:

```text
Browser → http://localhost:5173/api/v1/options
        → Vite proxy → http://localhost:8080/api/v1/options
        → JSON choices/defaults → controlled preferences form
```

The frontend calls the relative `/api` URL. Vite forwards API requests to Spring Boot without changing their paths. The browser talks to its own origin, so this development flow needs no backend CORS changes.

## Installed Dependency Versions

Use `npm ls --depth=0` to inspect installed direct dependencies. TanStack Query handles server data; React state holds the visitor's in-progress form selections.

| Package | Installed version |
| --- | --- |
| `@tanstack/react-query` | 5.105.0 |
| `@tailwindcss/vite` | 4.3.3 |
| `@testing-library/jest-dom` | 7.0.1 |
| `@testing-library/react` | 16.3.3 |
| `@testing-library/user-event` | 14.6.8 |
| `@types/node` | 24.19.2 |
| `@types/react` | 19.3.0 |
| `@types/react-dom` | 19.3.0 |
| `@vitejs/plugin-react` | 6.1.2 |
| `jsdom` | 30.1.3 |
| `oxlint` | 1.89.0 |
| `react` | 19.3.1 |
| `react-dom` | 19.3.1 |
| `tailwindcss` | 4.3.3 |
| `typescript` | 6.0.3 |
| `vite` | 8.3.5 |
| `vitest` | 5.0.5 |

## Verification Record

Verified on 27 October 2026:

- `npm test`: all 7 T05 component tests passed.
- `npm run build`: strict TypeScript checking and production asset generation passed.
- `npm run lint`: passed.
- `git diff --check`: passed.
- Manual localhost visual and keyboard review: not performed; follow the browser steps above.

Changes remain uncommitted for review.
