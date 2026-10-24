# Frontend — T02

React and TypeScript frontend for Meal Planner. This increment displays the backend connection status; meal-planning features come later.

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

Open **http://localhost:5173** in your browser. Keep both terminals running while checking the connection. Vite requires port 5173 to be free and exits with an error if it is occupied. Stop a server with Ctrl+C in its terminal.

PostgreSQL is required from T03 onward. No AI key is needed. Keep database passwords in the backend environment; never add secrets to frontend files or `VITE_` variables.

## Check the Page in Your Browser

1. With both applications running, open or refresh `http://localhost:5173`. Expect “Checking backend connection…” followed by “Backend connected.” The loading message may be brief on a fast connection.
2. Stop Spring Boot with Ctrl+C in terminal 1. Click **Retry connection** in the browser. Expect “Backend connection unavailable” and an explanation. The card checks on page load and retry; it does not poll automatically.
3. Restart Spring Boot with `./gradlew bootRun` in terminal 1. Wait for its startup message, then click **Retry connection**. Expect “Backend connected” again.
4. Resize the browser or use its mobile device toolbar. Check that the text and button fit at narrow widths.
5. Use Tab to focus **Retry connection** and Enter or Space to activate it. Focus should be visible. The button is disabled while a check is running.

Requests time out after five seconds. Connection status uses text as well as color and is announced through a polite live region. You can inspect the layout directly on localhost; screenshots are not required.

If a check fails, confirm that PostgreSQL is healthy and Spring Boot is running on port 8080. You can also open `http://localhost:5173/api/v1/health`; through the proxy, it should return `{"status":"UP"}`.

## Automated Checks

Run these from `frontend/`:

| Command | Purpose |
| --- | --- |
| `npm run dev` | Start the Vite development server |
| `npm test` | Run component tests once and exit |
| `npm run test:watch` | Watch files and rerun tests interactively; press `q` to exit |
| `npm run build` | Check strict TypeScript types and create production assets in `dist/` |
| `npm run lint` | Run Oxlint |

Component tests use Vitest, React Testing Library, jest-dom, and jsdom. They mock `fetch`, so neither server needs to be running. They cover loading, a valid health response, network and HTTP failures, malformed or unexpected data, timeout, retry, late responses, unmount cancellation, and React StrictMode cleanup. Separate unit tests will be useful when there is standalone application logic to test.

A production build verifies that assets compile. Production API hosting is part of T16; the development proxy is not packaged into `dist/`.

## How the Pieces Fit Together

- `src/main.tsx` mounts React with StrictMode.
- `src/App.tsx` renders the title, description, and connection card.
- `src/components/ConnectionCard.tsx` owns the health request and loading, connected, and error states. It uses native `fetch`, validates HTTP success and a JSON `status` of `UP`, and cancels requests on timeout or unmount. Retry starts a fresh check. TanStack Query is deferred to T05.
- `src/index.css` imports Tailwind. `vite.config.ts` registers its Vite plugin, following the [official Tailwind setup](https://tailwindcss.com/docs/installation/using-vite).
- `vite.config.ts` also sets port 5173, the API proxy, and the jsdom test environment. `src/test/setup.ts` installs DOM assertions and cleans up after tests.

The development request flow is:

```text
Browser → http://localhost:5173/api/v1/health
        → Vite proxy → http://localhost:8080/api/v1/health
        → JSON response → visible connection status
```

The frontend calls the relative URL `/api/v1/health`. Vite forwards `/api` requests to Spring Boot without changing the path. The browser talks to its own origin, so this development flow needs no backend CORS changes.

## Installed Dependency Versions

Scaffolded with the official Vite React TypeScript template using `create-vite` 9.3.1. These are the resolved direct dependency versions from `package-lock.json`; use `npm ls --depth=0` to inspect your installation.

| Package | Installed version |
| --- | --- |
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

Verified on 22 October 2026:

- Dependency installation completed successfully; npm reported zero known vulnerabilities at installation time.
- `npm test`: all 16 component tests passed.
- `npm run build`: strict TypeScript checking and production asset generation passed.
- `npm run lint`: passed.
- Local Chrome against real Vite and Spring Boot: connected state, error after backend shutdown, and successful retry after restart passed.
- Browser layout check at 375px: no horizontal overflow; retry could receive focus; no browser runtime errors were observed.

Follow the localhost steps above for your own visual and keyboard review. No screenshot comparison suite or browser-testing dependency was added to the project. Changes remain uncommitted for review.
