# Meal Planner & Grocery Shopping Assistant — Requirements

## 1. Product Goal and Scope

Build a learning and portfolio web app that helps visitors answer:

> “What can I cook today, and which grocery products do I need to buy?”

Visitors use the app without registering. They receive AI meal suggestions, select a recipe, and turn its ingredients into a shopping checklist with real Irish retailer product links.

### MVP

- One meal at a time.
- Cuisine, meal preferences, and serving sizes.
- AI-generated recipes.
- Ingredient-to-product matching from a curated catalog.
- Grocery checklist saved in the current browser.
- Clearly labeled sample mode without an AI API key.
- Responsive layouts for phones and desktops.

### Future Features

Weekly planning, accounts, shared lists, pantry inventory, nutrition targets, live prices and stock, price comparison, checkout integration, and payments are outside the MVP.

## 2. Business Flow and Functional Requirements

### Main Visitor Flow

1. **Enter preferences**
   - Cuisine: Any cuisine, American, Irish, or Vietnamese; default “Any cuisine.” Select one option per request.
   - Meal type: breakfast, lunch, or dinner.
   - Servings: 1–8; default 2.
   - Maximum cooking time: 15, 30, 45, or 60 minutes.
   - Diet: no preference, vegetarian, or vegan.
   - Ingredients to exclude, selected from the supported ingredient catalog.
   - Preferred retailer, or “All supported retailers.”

2. **Generate suggestions**
   - Return three meal suggestions with names, descriptions, cuisine, cooking times, and diet labels.
   - Use the selected cuisine to guide dish selection, ingredients, and preparation style.
   - “Any cuisine” allows suggestions from any supported cuisine.
   - Dietary restrictions, excluded ingredients, and cooking-time limits remain mandatory when a cuisine is selected.
   - Show loading, retry, empty-result, and service-unavailable states.
   - Preserve inputs when generation fails.
   - Regeneration replaces suggestions only after a successful response.

3. **Choose a recipe**
   - Display cuisine, ingredients, quantities, servings, and numbered cooking instructions.
   - Allow serving changes and scale ingredient quantities proportionally.
   - Keep generated recipe content stable when adjusting servings; another AI request is unnecessary.

4. **Create the grocery checklist**
   - Produce one checklist from the selected recipe.
   - Combine duplicate ingredients only when their units are compatible.
   - Let visitors mark ingredients as already owned or purchased.
   - Separate the recipe quantity from the retail pack size.
   - Offer plain-text copy and browser printing.

5. **Find products**
   - Show curated matching products, retailer names, pack sizes, and external product links.
   - Allow visitors to choose among available matches.
   - Clearly identify ingredients with no verified match.
   - Open retailer pages for visitors to confirm the item and complete any purchase.

6. **Return later**
   - Restore preferences, including cuisine, the selected recipe, and checklist from browser storage.
   - Provide a “Clear my data” action.
   - Explain that browser data does not synchronize across devices.

### Cuisine and Catalog Rules

- Begin with American, Irish, and Vietnamese cuisines. Maintain an extensible supported-cuisine list.
- Include common ingredients needed for all three cuisines in the initial catalog.
- Label recipes as “inspired by” the selected cuisine when adapting traditional dishes substantially.
- Do not silently change the requested cuisine when constraints cannot be satisfied. Explain the result and let visitors adjust preferences.
- Include sample recipes for every supported cuisine; return fewer suggestions when the sample catalog cannot satisfy all filters.

### Product Matching Rules

- Start with approximately 40 common ingredients and 60 verified product entries across two Irish retailers.
- Target Tesco Ireland and Dunnes Stores initially, subject to verifying usable product pages.
- Store canonical ingredient identities and aliases.
- Match by ingredient identity and product form. For example, canned tomatoes must not silently match fresh tomatoes.
- AI must never invent product names, retailer URLs, prices, or availability.
- Show suggested pack counts only when recipe and product units are compatible; otherwise ask the visitor to check the pack size.
- Display “Curated product link—check retailer for current details” and the last verification date.
- Do not display prices, availability guarantees, or estimated basket totals in v1.

### Catalog Maintenance Flow

The developer verifies product pages, records ingredient mappings and pack details, and updates the catalog through version-controlled seed data. An administration interface is outside the MVP.

## 3. Technology and Architecture

| Layer | Selected technology | Responsibility |
| --- | --- | --- |
| Frontend | React, TypeScript, Vite | Preferences, suggestions, recipes, checklist |
| UI | Tailwind CSS, React Router | Responsive styling and navigation |
| Server state | TanStack Query | API requests, loading, and error states |
| Backend | Java 21, Spring Boot, Gradle | Validation, AI orchestration, product matching |
| Persistence | PostgreSQL, Spring Data JPA | Ingredient and retailer catalog |
| Migrations | Flyway | Repeatable schema and catalog changes |
| AI | OpenAI Responses API via Spring RestClient | Structured meal generation |
| Tests | Vitest, React Testing Library, JUnit, Testcontainers, Playwright | Unit, integration, and workflow verification |
| Local environment | Docker Compose | PostgreSQL and repeatable full-stack startup |

Vite provides a React TypeScript starting point. Java 21 is within the documented Spring Boot compatibility range. Pin compatible stable dependencies when implementation begins. See the [Vite guide](https://vite.dev/guide/) and [Spring Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html).

**Architecture:** browser → Spring Boot REST API → PostgreSQL and AI provider. Browser requests never call the AI provider directly.

Use one repository with `frontend/`, `backend/`, and `docs/`. Develop locally first; later package the frontend and backend as containers behind one HTTPS origin.

### Data Ownership

- **PostgreSQL:** ingredients, aliases, retailers, products, ingredient-product mappings, and sample recipes tagged by cuisine.
- **Browser storage:** preferences, selected recipe, chosen products, and checklist progress.
- **Server memory:** generated responses during request processing; no permanent visitor profiles or recipe history.

### Minimum API Boundaries

- `GET /api/v1/options`: supported cuisines, other preferences, ingredients, retailers, and generation mode.
- `POST /api/v1/meal-suggestions`: validated preferences, including cuisine, in; three structured recipes and live/sample mode out.
- `POST /api/v1/product-matches`: recipe ingredients, quantities, and retailer preference in; curated matches and unmatched ingredients out.

Recipes contain a name, description, cuisine, servings, cooking time, diet labels, ingredient quantities and units, and cooking steps. Product results distinguish ingredient requirements from retail packs.

Return consistent validation and service errors without exposing internal exceptions or credentials.

## 4. AI, Reliability, and Privacy Requirements

- Use schema-constrained output and validate it again in Java. Enforce supported cuisine values, supported ingredients, positive quantities, cooking-time limits, and diet exclusions. Schema validation alone cannot establish culinary suitability; include reviewed cuisine examples in generation evaluation. See [OpenAI Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs?api-mode=responses).
- Supply the selected cuisine and supported ingredient catalog to generation.
- Reject invalid or incompatible results. Allow one bounded repair attempt, then return a clear failure.
- Keep provider credentials and the configured model name in backend environment variables.
- Apply a 30-second generation timeout and configurable anonymous request limits before calling the provider.
- Keep live generation disabled until credentials and usage controls are configured.
- Without credentials, run in labeled sample mode and filter recipes by all selected preferences.
- Never silently switch to sample content after a live request fails; offer an explicit sample-mode action.
- Treat dietary exclusions as preferences; do not claim certified allergen safety. Visitors must check retail product labels.
- Avoid logging preference payloads. Record request IDs, latency, validation failures, and provider errors.
- Support keyboard navigation, visible focus, labeled inputs, and readable mobile layouts.
- Handle unavailable browser storage gracefully by retaining state for the current session.

## 5. Delivery Milestones and Acceptance Criteria

### Milestone 1 — Catalog and Sample Experience

Establish the database, cuisine options, seed catalog, API contracts, and complete sample-mode workflow.

**Accept when:** a visitor can choose a cuisine and other preferences, select a sample meal, scale servings, generate a checklist, and open verified product links without logging in.

### Milestone 2 — Live AI Generation

Add server-side generation, validation, timeout handling, and request limits.

**Accept when:** valid requests produce three compatible suggestions; malformed output, provider failures, and exceeded limits produce recoverable errors.

### Milestone 3 — Portfolio Readiness

Complete accessibility checks, responsive layouts, automated tests, setup documentation, and a repeatable containerized demo.

### Required Test Scenarios

- American, Irish, and Vietnamese selections reach the backend and AI request correctly.
- Sample mode filters recipes by cuisine and restores cuisine after refresh.
- Unsupported cuisine values are rejected.
- Reviewed generation examples reflect the requested cuisine.
- Cuisine selection never overrides diet restrictions or ingredient exclusions.
- Conflicting preferences produce an explanation without silently changing cuisine.
- Serving changes scale quantities correctly.
- Duplicate compatible ingredients combine correctly.
- Incompatible units never produce misleading pack counts.
- Missing product matches remain visible as unmatched.
- Checklist progress survives refresh and clears on request.
- Sample mode works without provider credentials.
- AI failures preserve visitor inputs.
- No provider secret appears in frontend assets or network responses.

**Portfolio success criterion:** a reviewer can complete the cuisine-and-preference-to-shopping flow using sample mode, understand the separation between AI recipes and curated retailer data, and run the project from documented instructions.
