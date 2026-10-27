# Development Plan

## Goal and Working Agreement

Build the [requirements](REQUIREMENTS.md) using the [architecture](ARCHITECTURE.md), while learning how to collaborate with an AI agent. Work through setup, core backend capabilities, and complete features in reviewable steps. Tasks are sized around meaningful outcomes rather than a fixed time limit.

Each task follows **plan → implement → test → review → commit**:

1. **Select and plan:** choose one task. The agent inspects current code and proposes the approach, affected areas, dependencies, and acceptance checks. Discuss decisions before coding.
2. **Implement:** after the user requests implementation, make only the selected task's changes. Explain important concepts and tradeoffs as they become relevant.
3. **Test:** run checks appropriate to the behavior. Report passed, failed, skipped, and blocked checks separately, with commands the user can repeat.
4. **Review:** explain what changed and why, summarize the diff, and identify limitations. The user reviews the code and tries the feature locally.
5. **Commit:** commit only after the user requests it. Include only reviewed changes, report the commit hash, and update task status in that commit. Do not push automatically.
6. **Stop:** do not automatically begin the next task. Choose it together after the current work is reviewed.

Suggested prompts:

> Plan T01. Explain the decisions and acceptance checks before writing code.
>
> Implement the agreed T01 plan only, then run its checks.
>
> Walk me through the changes and help me test them locally.
>
> Commit the reviewed T01 changes.

Task statuses are **Planned → In progress → Ready for review → Committed**. Record blockers alongside the status. A task with required checks still blocked is not ready for review as a completed implementation; explain what remains and resolve or explicitly revise its acceptance criteria together.

## Security Working Agreement

Keep secrets in ignored backend environment configuration; `.env.example` contains placeholders only. Never hardcode, log, expose, or commit credentials, including in tests or documentation, and never put them in `VITE_` variables. Check changed/tracked files for accidental secrets before review and commit.

Secret-bearing third-party calls remain backend-only. Validate future API inputs, protect sensitive endpoints, and bound live AI usage before enabling it. Treat model outputs and external content as untrusted data; validate tool arguments, restrict permissions and execution steps, and cap spending. Obtain explicit approval for purchases, payments, destructive actions, and security-configuration changes. Prefer maintained dependencies and proportionate checks without unnecessary packages.

## Current Progress

| Tasks | Status | Notes |
| --- | --- | --- |
| T00 | Committed | Approved by the user; documentation committed as `b38576b` |
| T01 | Committed | Backend committed as `3558ce3`; existing health integration-test report records one passing test |
| T02 | Committed | Frontend committed as `06df3f1`; 16 component tests and build passed again during T03 |
| T03 | Committed | `4c4c15e` — catalog persistence, single-account simplification, and local startup convenience; 27 backend tests passed |
| T04 | Ready for review | Options API, reusable validation, and safe errors implemented; see verification record below; uncommitted |
| T05–T16 | Planned | Implementation has not begun |

T00–T03 are committed; T03 is `4c4c15e`. T04 is implemented and awaiting review. Each subsequent task still requires its own plan, implementation, tests, and review.

## Phase 0 — Agree on the Project

### T00 — Restore requirements and document the approach

- **Depends on:** the previous requirements and the clean Git clone.
- **Objective and changes:** copy requirements unchanged; write architecture, task sequence, phase gates, and the collaboration agreement.
- **Acceptance checks:** the copy matches its source; local links resolve; documents describe planned work; every MVP behavior maps to a task. Add no application code or dependencies.
- **Learn:** separating product requirements, architectural decisions, and implementation scope.
- **Suggested commit:** `Document requirements architecture and development roadmap`.

**Phase exit:** the user has reviewed the documents and requested their initial commit. Start T01 only after selecting and planning it separately.

## Phase 1 — Establish the Foundation

### T01 — Set up the Spring Boot backend

- **Depends on:** T00.
- **Objective and changes:** verify the Java/Gradle environment; create a Java 21 Spring Boot application, Gradle Wrapper, basic health endpoint, test setup, and backend run instructions. Add relevant ignore rules without committing credentials.
- **Acceptance checks:** the application starts without a database or AI key; the health endpoint responds; the wrapper runs a basic automated test. Record exact compatible dependency versions.
- **Learn:** application startup, dependency management, HTTP controllers, and the difference between a unit test and an application test.
- **Suggested commit:** `Set up Spring Boot backend with health endpoint`.

#### T01 Implementation and Verification Record

The source includes a Java 21 application, `GET /api/v1/health` returning `{"status":"UP"}`, an explicit response record, and an integration test that starts a real HTTP server on a random port. At the user's request, the build now uses Gradle's Groovy DSL and wrapper. Tooling is pinned to Spring Boot 3.5.16 and Gradle 8.14.3. See the [backend guide](backend/README.md) for the walkthrough and commands.

The following table preserves the earlier agent-side verification attempts; blocked checks are infrastructure results, not test failures. Later evidence is recorded separately below.

| Check | Result |
| --- | --- |
| Gradle build/settings syntax, Java target, dependency declarations, wrapper configuration, and executable permissions | Passed static checks; Groovy parsing does not verify plugin resolution or project configuration |
| `sh -n backend/gradlew` | Passed shell syntax check |
| Wrapper JAR SHA-256 and pinned distribution checksum | Matched Gradle's published reference |
| Wrapper `--version` with a cached distribution and isolated writable cache | Passed: Gradle 8.14.3 |
| Java source/test syntax parsing | Passed for all four Java files; not full compilation |
| `git diff --check` and new source whitespace checks | Passed |
| Gradle Wrapper `clean build` with an isolated cache and offline resolution | Blocked before project configuration: local file-lock communication socket creation returned `Operation not permitted` |
| JUnit integration test and application startup | Not run; JDK 21 and dependency access are required |
| Manual HTTP health request | Not run; application could not yet be started |
| Windows wrapper | Included from upstream; not executed on Windows |

During those earlier attempts, the agent shell exposed Java 17.0.9 and restricted downloads and sockets. Gradle 8.14.3 was available in the user's cache, but the isolated build could not start. No environment-specific workaround was added to project configuration.

T01 was subsequently committed as `3558ce3` (`set up backend`). The existing report at `backend/build/test-results/test/TEST-ie.mealplanner.health.HealthEndpointTest.xml`, timestamped `2026-10-21T22:30:23.053Z`, records **1 test, 0 failures, 0 errors, and 0 skipped**, using Java 21.0.12.1. This report is later evidence; it is not a rerun of the earlier blocked checks or proof of a fresh `clean build`. During T02 verification, the installed JDK 21 was selected explicitly, Spring Boot started with `./gradlew bootRun`, and the real health endpoint returned HTTP 200 with `{"status":"UP"}` through Vite.

### T02 — Set up React/Vite and connect to the backend

- **Depends on:** T01.
- **Objective and changes:** verify Node/npm; create the React TypeScript application, Tailwind setup, frontend test setup, and development API proxy. Display backend health in a simple page.
- **Acceptance checks:** the frontend starts and builds; a component test passes; the browser shows backend health and a useful error when it is unavailable. Retain the dependency lockfile.
- **Learn:** frontend tooling, components, API calls, and development proxies.
- **Suggested commit:** `Set up React frontend and backend connection`.

#### T02 Implementation and Verification Record

Implemented the official Vite React TypeScript template in `frontend/`, strict TypeScript checking, npm lockfile and Node >=22.12 requirement, Tailwind's Vite plugin, and a development proxy from port 5173 to Spring Boot on port 8080. The connection card uses native `fetch` with response validation, a five-second timeout, cancellation, accessible status announcements, and retry. No meal-planning features or backend CORS changes were added. TanStack Query remains planned for T05.

See the [frontend guide](frontend/README.md) for installed dependency versions, commands, and steps to inspect the app on localhost.

| Check | Result |
| --- | --- |
| Tooling and dependency installation | Passed with temporary Node 22.23.4 and npm 10.9.9; retained `package-lock.json`; npm reported zero known vulnerabilities at installation time |
| `npm test` | Passed: 16 component tests covering loading, success, failures, response validation, timeout, retry, stale responses, and cleanup |
| `npm run build` | Passed strict TypeScript checking and production asset generation |
| `npm run lint` | Passed |
| Real Chrome → Vite → Spring Boot | Passed with JDK 21: HTTP 200 and `status: UP`, visible connected state |
| Backend shutdown, restart, and browser retry | Passed: useful error while unavailable, then connected after restart and retry |
| Responsive browser inspection | Passed at 375px: no horizontal overflow, retry focusable, no browser runtime errors observed |
| User visual and keyboard review | Localhost steps documented for review; screenshot comparisons are not required |

These checks ran on 22 October 2026 with approved execution outside the read-only sandbox. The default shell lacked Node and selected Java 17; temporary Node tooling and an existing JDK 21 enabled verification. No dependency-download blocker remains for this verification run. The full backend `clean build` was not rerun. Browser checks used temporary tooling outside the repository; no screenshot testing suite was added. Production API hosting remains T16. T02 was subsequently committed as `06df3f1` (`set up frontend`). The user separately authorized T03 after agreeing its scope.

### T03 — Introduce PostgreSQL and catalog persistence

- **Depends on:** T01–T02.
- **Objective and changes:** add database-only Compose configuration, Flyway migrations, JPA entities/repositories, and a small seed set for ingredients, retailers, products, and reviewed mappings. Keep this dataset small and explicitly distinguish verified links from test fixtures.
- **Acceptance checks:** migrations apply to a fresh PostgreSQL database; keys and mappings are valid; repository integration tests run with Testcontainers; restarts preserve data. Configure the health/startup checks appropriately once persistence is required.
- **Learn:** relational modeling, migrations, environment configuration, and integration testing.
- **Suggested commit:** `Add PostgreSQL catalog persistence and migrations`.

#### T03 Implementation and Verification Record

Implemented database-only Compose with PostgreSQL 17.11, a named volume and localhost-only port, Flyway schema/seed migrations, five catalog models plus their repositories, and bounded database-aware health. The existing success contract remains HTTP 200 with `{"status":"UP"}`; a database outage now returns HTTP 503 with `{"status":"DOWN"}`. Hibernate validates the Flyway-managed schema; Open Session in View is disabled. Product lookup uses bound ingredient/retailer parameters and does not expose entities through HTTP.

The agreed seed contains **20 ingredients, 10 aliases, 2 retailers, 12 real products, and 12 reviewed mappings**. Six ingredient types have a verified product at each retailer; fourteen identities remain unmapped. [Catalog sources and review decisions](backend/CATALOG.md) document retailer pages, dated verification, tomato juice quantities, and unknown fixed quantities for loose onions. Shared ingredients do not add French or Italian cuisine choices. The larger 40-ingredient/60-product target remains T14.

Passwords have no committed defaults. Compose requires private configuration; the backend guide generates secrets directly into an ignored `.env` without printing them. The original implementation separated a read-only account and a migration owner; on 24 October the user requested the simpler single non-superuser backend account described below. Test credentials remain generated at runtime. No personal `.env` was created during verification.

| Check | Result |
| --- | --- |
| JDK 21 / Gradle backend build | Passed; final `./gradlew build` executed the tests after the SQL-bootstrap change |
| JUnit/Testcontainers | 25 passed, 0 failures/errors/skips: 23 catalog/constraint/permission cases, 1 real HTTP health case, 1 sanitized failure case |
| Fresh PostgreSQL migrations and JPA validation | Passed against disposable PostgreSQL 17.11 and an isolated Compose database |
| Seed integrity and repository lookup | Passed exact counts, distinct forms, aliases, reviewed relationships, optional retailer filtering, and unknown pack values |
| Database constraints | Passed rejection of duplicates, missing references, missing required fields, unsafe URL schemes, and invalid pack quantity/unit pairs |
| Original account checks (22 October) | Passed read-only enforcement; superseded by the user-approved single-account change below |
| Migration rerun | Passed: no pending migrations or duplicate seed rows |
| Real database outage/recovery | Passed: HTTP 503 `DOWN` in 1.01 seconds after stopping PostgreSQL, then HTTP 200 `UP` after restart without backend restart |
| Named-volume persistence | Passed: a temporary test row survived container removal/recreation; after removing that fixture and restarting the app, counts remained 20/12/12 with two successful migrations |
| Frontend `npm test` and `npm run build` | Passed: all 16 component tests, strict TypeScript checking, and production assets |
| Frontend `npm audit` | Reported 0 known vulnerabilities for the installed dependency graph at check time |
| Repository secret check | No tracked private `.env`/private-key paths or recognizable API-key/token/private-key/credential-in-URL patterns found; heuristic inspection, not a formal security audit |
| Ignore rules | Confirmed root/backend/frontend `.env` variants are ignored; `.env.example` remains reviewable |
| Dedicated backend dependency vulnerability scanner | Not run; dependencies remain managed by the existing Spring Boot BOM; no scanner service/package added |
| Human localhost visual/keyboard review | Run steps documented; no screenshot testing required |

Checks ran on 22 October 2026 with approved access outside the read-only sandbox. The first Compose check found a mounted-shell-script execution failure; initialization was changed to SQL, the backend tests reran, and fresh Compose startup and lifecycle checks then passed. Only isolated verification containers and volumes were removed with approval; the earlier draft database volume and personal configuration were preserved. The earlier draft PostgreSQL container was stopped with approval after verification, freeing port 5432 while preserving its volume. No data deletion or credential rotation was performed on that draft database.

See the [backend guide](backend/README.md) and updated [frontend localhost guide](frontend/README.md) for setup and repeatable checks. There are no live AI calls, keys, paid actions, new cuisine choices, or public catalog endpoints in T03. T03 was subsequently committed as `4c4c15e` (`add catalog persistence and seed data`), including the follow-up changes below. T04 was separately authorized through its implementation plan.

#### T03 Single-Account Simplification — 24 October 2026

At the user's request, Flyway now uses the same datasource as normal queries: non-superuser `meal_planner_app` with `APP_DB_PASSWORD`. Fresh database initialization makes this account the application database owner. Backend startup instructions explicitly remove the Docker bootstrap administrator credentials from the process environment. The private root `.env` still contains the separate Docker bootstrap password for initial server setup; no personal secrets or passwords were read into tool output, changed, or committed.

Applied V1/V2 migrations remain byte-for-byte unchanged. A separate, explicit ownership-upgrade SQL transaction supports existing local databases without deleting data, altering passwords, or repairing Flyway checksums. Only the current application database and its named catalog/history tables change owners. Unrelated tables are untouched. Single-account tests cover repository writes, constraints, schema changes, and the absence of superuser/role-creation/database-creation privileges. A dedicated legacy-database test verifies that existing rows, identity sequences, and Flyway history survive the change and migration validation still passes.

Verification on 24 October: `./gradlew build` passed with **27 tests, 0 failures, 0 errors, and 0 skipped**. This includes a fresh single-account database and an upgrade from the original two-account schema. The existing local database ownership transfer was then applied with explicit approval. Before/after comparisons confirmed every row in all five catalog tables and Flyway history was unchanged; all six tables and the application database now belong to `meal_planner_app`. Its superuser, database-creation, role-creation, replication, and RLS-bypass flags are all disabled. Personal `.env`, passwords, volumes, and unrelated tables were not modified. `git diff --check` passed. Frontend application code was unchanged, so its previously passing tests were not repeated for this account-only change.

Restart any already running backend using the updated README commands, which remove bootstrap administrator credentials before starting Gradle. The local database upgrade has already been completed; no reset or repeat upgrade is needed on this machine. These changes were subsequently included in T03 commit `4c4c15e`; no T04 work was included in that commit.

#### Local Startup Convenience — 24 October 2026

At the user's request, new zsh terminals on this Mac now select the existing JDK 21 through a guarded `~/.zshrc` entry; the previous shell configuration was backed up outside the repository. No secrets are stored in shell startup files.

`./gradlew bootRun` now reads only approved settings from the ignored root `.env`, passes the application password and derived/explicit database URL to Spring Boot, and excludes Docker administrator credentials even when they were exported beforehand. Literal values and optional matching quotes are supported; the file is never executed as shell code. Explicit environment settings take precedence. Tests/builds do not load the local file. README startup commands no longer require repeated exports or sourcing.

Verification: a fresh login shell reported Java/compiler 21; the backend build passed with existing tests up-to-date. Temporary runtime probes passed local file loading, environment precedence, and administrator-credential exclusion. A real `bootRun` launched from a fresh shell with no manual database exports returned HTTP 200 `UP` against local PostgreSQL. Only that temporary verification backend was stopped. `.env`, passwords, database contents, and other servers were unchanged. `git diff --check` passed. No new dependency or committed machine-specific Java path was added.

### T04 — Define preference rules and the options API

- **Depends on:** T03.
- **Objective and changes:** introduce explicit DTOs, core preference validation, recipe/ingredient types, consistent error responses, and `GET /api/v1/options`. Return supported cuisines, meal types, diets, time choices, ingredient exclusions, retailers, and generation mode.
- **Acceptance checks:** options come from agreed configuration/catalog data; unit tests reject unsupported choices and out-of-range values; API tests verify the response shape. Do not expose JPA entities.
- **Learn:** contracts, DTOs, controller/service boundaries, and business validation.
- **Suggested commit:** `Add preference validation and options API`.

#### T04 Implementation and Verification — 25 October 2026

Implemented `GET /api/v1/options` using controller → read-only service transaction → catalog repositories and explicit record DTOs. Fixed cuisine/meal/diet choices are defined by shared code/label enums; serving bounds and cooking times are shared with validation. The response includes exact defaults, sample mode, all 20 ingredients including unmapped entries, and both actual retailers, sorted by name then code. Internal IDs, aliases, products, website URLs, and provider configuration are omitted.

Added the Boot-managed validation starter, immutable/deduplicated preference requests, reusable catalog-aware `PreferenceValidator`, recipe-ingredient decimal quantity validation, and quantity units G/KG/ML/L/EACH. Persistence `PackUnit` and applied migrations are unchanged. `any` remains a preference sentinel, not a concrete recipe cuisine. T06 must reuse the validator before suggesting recipes and validate nested recipe values with `@Valid`; no validation endpoint or full recipe response was introduced.

Safe error records carry code, message, a fresh UUID request ID, and field/message pairs. Tests verify 400 invalid preferences, 503 database query/transaction failures, 500 unexpected failures, and preserved MVC statuses/headers. Error bodies omit rejected values and private exception details. The health endpoint still returns its original UP/DOWN contract.

Verification: `./gradlew build` passed on JDK 21 with **45 tests, 0 failures, 0 errors, 0 skipped** (27 existing and 18 added). Testcontainers verified real HTTP options and catalog validation against disposable PostgreSQL 17.11. Fixed choices/defaults, serving boundaries, required/unsupported values, malformed request binding, unknown catalog codes, empty/deduplicated exclusions, and nested positive decimal quantities passed. Mock failures tested safe responses without interrupting the user's database. Existing catalog constraints, ownership upgrade, migrations, and health tests passed. `git diff --check` passed. Gradle/Docker verification needed approved access outside the filesystem sandbox. The documented localhost curl command was not separately run; real HTTP verification used the test application's random port.

The [backend guide](backend/README.md#preference-options-and-validation) documents the contract and `curl --fail-with-body http://localhost:8080/api/v1/options`. T05 will implement search-and-select with removable exclusion tags, filtering the downloaded catalog by name/form. Arbitrary free text, alias search, and server-side ingredient search remain deferred. Exclusions describe unwanted recipe ingredients; owned groceries remain a separate checklist feature, and exclusions do not guarantee allergy protection.

No frontend implementation, new migration, recipe generation, AI calls, or local secret/configuration changes were made. Live generation remains disabled. T04 is uncommitted for review; suggested commit: `Add preference validation and options API`. T05 has not begun.

**Phase exit:** both applications run locally, the browser reaches the backend, PostgreSQL is initialized reproducibly, and the options contract has automated checks. Block further work if required tooling is still unavailable.

## Phase 2 — Discover Meals with Sample Data

### T05 — Deliver the preference form

- **Depends on:** T04.
- **Objective and changes:** build labeled controls for cuisine, meal type, servings, cooking time, diet, exclusions, and retailer choice using backend options. Use search-and-select with removable tags for exclusions: filter the downloaded ingredient catalog by name/form as visitors type and add only confirmed code selections. Add an “All supported retailers” choice for null retailer. Free text, alias search, and server-side ingredient search are deferred. Add TanStack Query for server state as API-driven features are introduced.
- **Acceptance checks:** American, Irish, Vietnamese, and Any cuisine can be selected; defaults and input bounds match requirements; options-loading failures can be retried; component tests cover important interactions. Meal generation is not part of this task.
- **Learn:** controlled inputs, accessible forms, and frontend/server validation responsibilities.
- **Suggested commit:** `Add meal preference form using backend options`.

### T06 — Deliver sample suggestions end to end

- **Depends on:** T05.
- **Objective and changes:** persist representative sample recipes, add the suggestion-provider boundary and sample implementation, expose `POST /api/v1/meal-suggestions`, and display results from the form. Cover every supported cuisine and meal type with sample data.
- **Acceptance checks:** cuisine, diet, exclusions, servings, and time constraints are honored; samples return up to three compatible results with an explicit sample label; impossible combinations give an honest empty state. Loading and errors preserve inputs and prior successful results.
- **Learn:** an end-to-end feature, provider interfaces, filtering, and boundary tests.
- **Suggested commit:** `Add sample meal suggestions end to end`.

### T07 — Add recipe details and serving adjustment

- **Depends on:** T06.
- **Objective and changes:** introduce recipe navigation, ingredient details, cooking steps, and serving controls. Scale from original recipe quantities so repeated adjustments do not accumulate rounding errors.
- **Acceptance checks:** selecting a meal shows its details; changing servings updates quantities correctly without another generation request; missing selection has a usable navigation path. Add calculation and component tests.
- **Learn:** routing, derived state, immutable data, and pure calculation tests.
- **Suggested commit:** `Add recipe details and serving adjustment`.

**Phase exit:** without an AI key, a visitor can choose preferences, get compatible sample meals, select a recipe, and adjust it for the people eating.

## Phase 3 — Turn Recipes into Shopping Actions

### T08 — Generate and manage the grocery checklist

- **Depends on:** T07.
- **Objective and changes:** create the selected recipe's checklist, combine duplicate compatible quantities, and track owned/purchased items. Define how selecting another meal replaces the current checklist so users do not lose progress unexpectedly.
- **Acceptance checks:** kg/g and l/ml combine correctly; count, volume, and weight remain separate; owned and purchased states can be toggled; serving changes produce correct requirements. Test quantities and checklist interactions.
- **Learn:** business calculations, state transitions, and testing invariants.
- **Suggested commit:** `Add grocery checklist with safe quantity aggregation`.

### T09 — Restore the visitor's plan across reloads

- **Depends on:** T08.
- **Objective and changes:** add versioned browser persistence for preferences, the selected recipe, servings, and checklist progress; include product choices when T10 introduces them. Add a clear-data action and explain browser-only storage.
- **Acceptance checks:** refresh restores the plan; clearing data resets it; malformed, obsolete, or denied storage does not break the app; late requests cannot restore cleared visitor data.
- **Learn:** persistence boundaries, data validation, and lifecycle edge cases.
- **Suggested commit:** `Persist visitor plans in browser storage`.

### T10 — Match ingredients to retailer products

- **Depends on:** T03, T08–T09.
- **Objective and changes:** implement `POST /api/v1/product-matches` and its UI. Add retailer filtering, product alternatives, compatible pack calculations, saved product choices, verification dates, and real external product links from the small verified catalog.
- **Acceptance checks:** exact ingredient forms match reviewed mappings; pack counts round upward only for compatible quantities; unknown yields request manual checking; unmatched ingredients remain visible; links and retailer filters work. Verify units in isolation and matching against PostgreSQL.
- **Learn:** domain identity, matching rules, database queries, and external-data limitations.
- **Suggested commit:** `Add curated retailer product matching`.

### T11 — Export the grocery checklist

- **Depends on:** T10.
- **Objective and changes:** add plain-text copying, recovery when clipboard access is denied, and a readable print layout.
- **Acceptance checks:** exports include required quantities and checklist states; copying or manual-copy recovery works; browser print preview is usable on desktop and mobile layouts.
- **Learn:** browser APIs, graceful failure, and manual verification of browser-specific behavior.
- **Suggested commit:** `Add grocery list copy and print actions`.

**Phase exit:** the complete shopping flow works with a representative verified catalog: preferences → sample recipe → scaled checklist → product links → export. The final catalog-size target remains tracked in T14.

## Phase 4 — Add Live AI Safely

### T12 — Add the live suggestion adapter

- **Depends on:** T06, T10.
- **Objective and changes:** implement a backend-only live provider using environment configuration, structured output, and the existing recipe contract. Validate generated content against supported ingredients and preferences. Keep the live path disabled by default and outside the normal visitor flow until T13 is complete.
- **Acceptance checks:** mocked provider tests cover valid, malformed, incomplete, refused, and incompatible responses; cuisine and exclusions reach the provider; credentials and provider internals do not enter frontend responses. The sample experience still works without a key.
- **Learn:** external-service adapters, structured output, mocking, and validation beyond JSON structure.
- **Suggested commit:** `Add structured live meal suggestion adapter`.

### T13 — Complete guarded live generation and recovery

- **Depends on:** T12.
- **Objective and changes:** expose configured live mode through the existing UI, enforce configurable anonymous usage limits, apply the 30-second generation timeout, and allow at most one repair attempt for invalid output. Provide an explicit sample action after failure and request/status/error-category logging without preference payloads.
- **Acceptance checks:** limits apply before provider calls; timeout and repair are bounded; errors preserve preferences and never silently substitute samples. Test with mocks and a controllable clock where needed. When credentials are available, run a separately identified live smoke test and review meal quality.
- **Learn:** reliability, anonymous resource limits, observability, and the distinction between deterministic tests and model evaluation.
- **Suggested commit:** `Add guarded live generation and explicit recovery`.

**Phase exit:** sample and live modes share the same visitor flow; configured live generation has tested validation, limits, and failure recovery. Record any unperformed real-provider checks explicitly.

## Phase 5 — Prepare the Portfolio Demo

### T14 — Expand and verify catalog coverage

- **Depends on:** T10.
- **Objective and changes:** expand toward approximately 40 ingredients and 60 verified products across two Irish retailers, targeting Tesco Ireland and Dunnes Stores subject to usable product pages. Cover useful ingredients across all three cuisines rather than filling the catalog with redundant alternatives. Add developer maintenance instructions and catalog checks.
- **Acceptance checks:** real URLs, pack labels, verification dates, mappings, aliases, and sample references pass review; no fabricated products or unverified placeholder links appear in visitor results; applied migrations remain immutable. Product information makes no live stock or price claims.
- **Learn:** data quality, provenance, coverage, and safe catalog migrations.
- **Suggested commit:** `Expand verified Irish retailer catalog`.

### T15 — Review accessibility and complete visitor journeys

- **Depends on:** T11, T13–T14.
- **Objective and changes:** refine responsive layouts, keyboard interaction, focus, labels, empty/error states, and recipe dietary notices. Add Playwright journeys for sample mode, restoration, clear-data behavior, retailer matching, and mocked live failures.
- **Acceptance checks:** critical desktop and mobile journeys pass; manual keyboard and print checks are recorded; changing cuisine or restrictions never silently presents incompatible replacement results as current. Review generated examples for culinary suitability separately from structural validation.
- **Learn:** browser tests, accessibility review, and complete behavior-based acceptance testing.
- **Suggested commit:** `Verify responsive and accessible meal planning journeys`.

### T16 — Package and automate verification

- **Depends on:** T15.
- **Objective and changes:** add frontend/backend container builds, a full-stack Compose setup behind one origin, CI checks, and a setup/run/test guide. Keep database-only development available. Document actual environment requirements and provider configuration.
- **Acceptance checks:** a fresh environment can build and run sample mode from documented commands; CI runs frontend, backend, database, and browser checks; skipped checks are visible; secrets remain outside source and frontend assets. Hosting and publishing require a separate task.
- **Learn:** reproducible builds, continuous integration, container networking, and release readiness.
- **Suggested commit:** `Add full-stack containers CI and developer guide`.

**Phase exit:** a reviewer can run the documented sample demo, complete the main flow, and inspect passing checks. Live AI remains optional; deployment, accounts, checkout, and other future features are not part of this roadmap.

## Requirements Coverage

| Requirement group | Owning tasks |
| --- | --- |
| Selected frontend/backend/database stack and local setup | T01–T04, T16 |
| Cuisine and other preferences | T04–T06 |
| Sample mode and compatible meal suggestions | T06 |
| Recipe details and serving scaling | T07 |
| Checklist aggregation and owned/purchased states | T08 |
| Browser persistence, clear data, and storage failure | T09–T10 |
| Retailer filtering, product alternatives, exact forms, safe pack counts, unmatched items | T10 |
| Copy and print | T11 |
| Structured AI output, credentials, and business validation | T12 |
| Timeout, request limits, repair, explicit recovery, and safe logs | T13 |
| Catalog size, two retailers, aliases, verification, and maintenance | T03, T14 |
| Accessibility, mobile usability, dietary notices, and full journeys | T05, T15 |
| Automated checks, reproducible startup, and handoff documentation | Every implementation task; final integration in T16 |

Tests are part of each feature, not deferred to the final phase. Before starting any task, inspect the actual repository and agree on its detailed interfaces and checks; this roadmap does not authorize implementing every future task at once.
