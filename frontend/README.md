# Frontend — T05

React and TypeScript frontend for Meal Planner. It loads supported choices from the backend and lets visitors enter meal preferences. Meal generation and browser persistence are later tasks.

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
2. Select American, Irish, Vietnamese, and Any cuisine; try the meal, serving, cooking-time, diet, and retailer controls. “All supported retailers” is sent as a null retailer preference in a later task.
3. Search exclusions by ingredient name or form. Choose a result to add it as a removable tag. A selected ingredient no longer appears in the results until removed.
4. Stop Spring Boot with Ctrl+C in terminal 1. Refresh the page; expect a useful options-loading error and **Retry loading options** button. Restart Spring Boot and retry.
5. Resize the browser or use its mobile device toolbar. Use Tab to reach each input, result, tag removal button, and retry button; focus should be visible.

T05 collects preferences only. It does not request meal suggestions or save choices across reloads. Those are later tasks.

## Automated Checks

Run these from `frontend/`:

| Command | Purpose |
| --- | --- |
| `npm run dev` | Start the Vite development server |
| `npm test` | Run component tests once and exit |
| `npm run test:watch` | Watch files and rerun tests interactively; press `q` to exit |
| `npm run build` | Check strict TypeScript types and create production assets |
| `npm run lint` | Run Oxlint |

Component tests use Vitest, React Testing Library, jest-dom, and jsdom. They mock `fetch`, so neither server needs to be running. They cover options loading and errors, retry, malformed response handling, backend-provided defaults and bounds, preference selection, and searching, adding, and removing ingredient exclusions.

A production build verifies that assets compile. Production API hosting is part of T16; the development proxy is not packaged into `dist/`.

## How the Pieces Fit Together

- `src/main.tsx` mounts React with StrictMode and the shared TanStack Query client.
- `src/App.tsx` renders the page header and preferences feature.
- `src/api/options.ts` fetches `/api/v1/options` and checks the response shape before the form uses it.
- `src/components/PreferencesPage.tsx` uses TanStack Query to load and cache options, show loading/error states, and let visitors retry.
- `src/components/PreferencesForm.tsx` owns the controlled form values and filters the downloaded ingredient catalog by name/form. Exclusions store selected catalog codes; the UI shows removable tags.
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
