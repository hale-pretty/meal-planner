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

## Current Progress

| Tasks | Status | Notes |
| --- | --- | --- |
| T00 | Committed | Approved by the user; documentation committed as `b38576b` |
| T01 | In progress | Backend migrated to Gradle; wrapper bootstrap and static checks passed; application verification blocked by environment restrictions and missing JDK 21; uncommitted |
| T02–T16 | Planned | Implementation has not begun |

The repository was cloned empty. T00 is now committed, and T01 is the active task. Each subsequent task still requires its own plan, implementation, tests, and review.

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

The agent environment exposes Java 17.0.9 and restricts dependency-download access and local sockets. Gradle 8.14.3 was available in the user's existing cache. The wrapper check reused that distribution through an isolated writable cache with native services disabled; the application build still could not start. No environment-specific Gradle workaround was added to project configuration. Resolve the environment requirements, run `./gradlew clean build` from `backend/`, start the application with `./gradlew bootRun`, and verify the endpoint before marking T01 ready for review. T01 has not been committed.

### T02 — Set up React/Vite and connect to the backend

- **Depends on:** T01.
- **Objective and changes:** verify Node/npm; create the React TypeScript application, Tailwind setup, frontend test setup, and development API proxy. Display backend health in a simple page.
- **Acceptance checks:** the frontend starts and builds; a component test passes; the browser shows backend health and a useful error when it is unavailable. Retain the dependency lockfile.
- **Learn:** frontend tooling, components, API calls, and development proxies.
- **Suggested commit:** `Set up React frontend and backend connection`.

### T03 — Introduce PostgreSQL and catalog persistence

- **Depends on:** T01–T02.
- **Objective and changes:** add database-only Compose configuration, Flyway migrations, JPA entities/repositories, and a small seed set for ingredients, retailers, products, and reviewed mappings. Keep this dataset small and explicitly distinguish verified links from test fixtures.
- **Acceptance checks:** migrations apply to a fresh PostgreSQL database; keys and mappings are valid; repository integration tests run with Testcontainers; restarts preserve data. Configure the health/startup checks appropriately once persistence is required.
- **Learn:** relational modeling, migrations, environment configuration, and integration testing.
- **Suggested commit:** `Add PostgreSQL catalog persistence and migrations`.

### T04 — Define preference rules and the options API

- **Depends on:** T03.
- **Objective and changes:** introduce explicit DTOs, core preference validation, recipe/ingredient types, consistent error responses, and `GET /api/v1/options`. Return supported cuisines, meal types, diets, time choices, ingredient exclusions, retailers, and generation mode.
- **Acceptance checks:** options come from agreed configuration/catalog data; unit tests reject unsupported choices and out-of-range values; API tests verify the response shape. Do not expose JPA entities.
- **Learn:** contracts, DTOs, controller/service boundaries, and business validation.
- **Suggested commit:** `Add preference rules and options API`.

**Phase exit:** both applications run locally, the browser reaches the backend, PostgreSQL is initialized reproducibly, and the options contract has automated checks. Block further work if required tooling is still unavailable.

## Phase 2 — Discover Meals with Sample Data

### T05 — Deliver the preference form

- **Depends on:** T04.
- **Objective and changes:** build labeled controls for cuisine, meal type, servings, cooking time, diet, exclusions, and retailer choice using backend options. Add TanStack Query for server state as API-driven features are introduced.
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
