# Backend — T04

Spring Boot backend with a PostgreSQL catalog, Flyway migrations, JPA repositories, a database-aware health endpoint, and sample meal suggestions. PostgreSQL is required; no AI key is needed. The catalog contains 28 ingredients, 12 sample recipes, and 12 reviewed retailer products. See [catalog sources and maintenance](CATALOG.md).

## Toolchain

| Tool | Project version | Where configured |
| --- | --- | --- |
| JDK | 21 | Java toolchain in `build.gradle` |
| Spring Boot | 3.5.16 | Plugin in `build.gradle` |
| Gradle | 8.14.3 | `gradle/wrapper/gradle-wrapper.properties` |
| PostgreSQL | 17.11 | Root `compose.yaml` and integration-test container |
| Flyway | 11.7.2 | Spring Boot dependency BOM |
| PostgreSQL JDBC | 42.7.11 | Spring Boot dependency BOM |
| Spring Data JPA | 3.5.13 | Spring Boot dependency BOM |
| Hibernate | 6.6.53.Final | Spring Boot dependency BOM |
| Testcontainers | 1.21.4 | Spring Boot dependency BOM |

The project uses Gradle's Groovy DSL: `build.gradle` defines the build, and `settings.gradle` names the project. Spring Boot's dependency BOM supplies compatible versions for the Web and Test dependencies through Gradle's native platform support. See [Spring Boot dependency management](https://docs.spring.io/spring-boot/3.5/gradle-plugin/managing-dependencies.html) and [supported build tools](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

Install a JDK 21 distribution and ensure `JAVA_HOME` points to it. Open a terminal in this directory and check:

```sh
java -version
javac -version
./gradlew --version
```

The Java and compiler versions should show 21. The wrapper should report Gradle 8.14.3. Gradle itself can launch on JDK 17, but this project's compilation and tests require its Java 21 toolchain. A successful `--version` check alone does not verify the application.

A global Gradle installation is unnecessary. The committed wrapper downloads the pinned distribution on first use and caches it under the Gradle user directory. Dependencies also need network access to the Gradle Plugin Portal and Maven Central. Maven Central is an artifact repository used by Gradle here, not the project's build tool.

The Java toolchain selects the compiler and test runtime. Automatic JDK downloading is not configured, so install JDK 21 locally rather than lowering the project's Java target.

On macOS, after installing and registering JDK 21, you can select it for the current terminal:

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
```

For SDKMAN-managed installations, use `sdk use java <installed-jdk-21-identifier>` instead. Confirm the active version before running the build.

On this development Mac, Java 21 is now selected automatically by a guarded entry in `~/.zshrc`. Open a new terminal to use it; you do not need to repeat the exports above. This machine-specific shell setting is outside Git and contains no database secrets. Other machines still need JDK 21 configured locally.

## Start PostgreSQL

Install and start Docker Desktop (or a compatible Docker engine with Compose). Check:

```sh
docker info
docker compose version
```

From the **repository root**, create your ignored local configuration once. This command generates secrets directly into `.env` with owner-only file permissions; it prints neither value and refuses to overwrite an existing file. `APP_DB_PASSWORD` is the only backend database password. `POSTGRES_PASSWORD` is retained solely for Docker bootstrap administration:

```sh
python3 - <<'PY'
import os
import secrets
from pathlib import Path

content = Path('.env.example').read_text()
for key in ('POSTGRES_PASSWORD', 'APP_DB_PASSWORD'):
    content = content.replace(key + '=\n', key + '=' + secrets.token_hex(32) + '\n')
fd = os.open('.env', os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
with os.fdopen(fd, 'w') as local_config:
    local_config.write(content)
PY
```

`.env.example` contains placeholders only. Keep `.env` private and ignored; do not paste it into chat, logs, Git, or frontend variables. Never prefix a secret with `VITE_`.

Start the database from the repository root:

```sh
docker compose config --quiet
docker compose up -d --wait
docker compose ps
```

PostgreSQL is published only on `127.0.0.1:5432`. The named `catalog_data` volume preserves data when containers stop or are recreated. The initialization SQL creates `meal_planner_app` as a non-superuser and makes it the database owner. Flyway and normal backend queries use that same account; Flyway creates and seeds the catalog when the backend starts.

Both Compose and `./gradlew bootRun` read the root `.env` automatically. The Gradle task passes only the application password and database URL to Spring Boot and removes exported Docker administrator credentials. You do not need to source `.env` in each terminal. Tests and builds do not load local credentials.

The supported file format is literal `KEY=value` with optional matching single/double quotes and full-line comments. Values are not executed or expanded as shell commands. Existing environment variables override file values. The database URL is derived from `POSTGRES_PORT` and `POSTGRES_DB`, unless `DATABASE_URL` is explicitly set. The URL should not contain credentials.

Passwords are established when the volume is first initialized. Editing `.env` alone does not rotate existing database passwords. Preserve existing data and perform a deliberate credential rotation rather than deleting a volume automatically.

## Upgrade an Existing Two-Account T03 Database

Fresh database volumes need no upgrade. If you already ran T03 with a read-only application account, stop the backend and run this once from the repository root using the existing database container:

```sh
docker compose exec -T postgres sh -c 'exec psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < backend/src/main/resources/database/upgrade-single-account.sql
```

This transaction transfers ownership of the current application database, its five catalog tables, and Flyway history to `meal_planner_app`. PostgreSQL transfers their dependent identity sequences too. It does not change passwords, delete rows, modify unrelated tables, or rewrite applied migrations. The script can be rerun safely. Do not use a database reset or Flyway repair for this change. An agent must obtain approval before applying this ownership change to your existing database.

Then restart the backend using the commands below. Keep using username `meal_planner_app` and your existing `APP_DB_PASSWORD` in pgAdmin; the account can now edit catalog data as well as read it.

## Run and Check the Endpoint

From the repository root:

```sh
cd backend
./gradlew bootRun
```

`bootRun` loads local settings only for the backend process; it does not export passwords into your terminal or frontend. For a direct IDE launch, configure only `APP_DB_PASSWORD` and, if needed, `DATABASE_URL`. Do not import the Docker administrator password into the backend process.

Spring Boot uses port 8080 by default. In a second terminal:

```sh
curl --fail-with-body -i http://localhost:8080/api/v1/health
```

Expected: HTTP 200, a JSON content type, and this body:

```json
{"status":"UP"}
```

The endpoint runs a bounded `SELECT 1` using the application account. When PostgreSQL is unavailable it returns HTTP 503 with `{"status":"DOWN"}` and no connection details. Missing database access or failed migrations prevent successful startup. It makes no AI-provider connectivity claim. The backend binds to localhost by default. Stop it with Ctrl+C.

To check recovery, keep the backend running, then from the repository root run `docker compose stop postgres`. Repeat the HTTP request; expect 503 and `DOWN` within five seconds. Run `docker compose up -d --wait`, then repeat it; expect 200 and `UP` without restarting the backend. The frontend also supports retry after this outage.

## Preference Options and Validation

With the backend running, check:

```sh
curl --fail-with-body http://localhost:8080/api/v1/options
```

`GET /api/v1/options` takes no preference input. It returns explicit record DTOs from controller → read-only transactional service → repositories:

| Field | Contract |
| --- | --- |
| `cuisines` | `{code,label}` entries: `any` (Any cuisine), `american` (American), `irish` (Irish), `vietnamese` (Vietnamese) |
| `mealTypes` | `{code,label}` entries: `breakfast`, `lunch`, `dinner`; title-case labels |
| `diets` | `{code,label}` entries: `none`, `vegetarian`, `vegan`; title-case labels |
| `servings` | `{"min":1,"max":8}` |
| `maxCookingTimeMinutes` | `[15,30,45,60]` |
| `ingredients` | All catalog ingredients, each with only `code`, `name`, `form`, ordered by name then code |
| `retailers` | Actual retailers only, each with only `code`, `name`, ordered by name then code |
| `generationMode` | `"sample"` |
| `defaults` | The preference object below |

```json
{
  "cuisine": "any",
  "mealType": "dinner",
  "servings": 2,
  "maxCookingTimeMinutes": 30,
  "diet": "none",
  "excludedIngredientCodes": [],
  "retailerCode": null
}
```

The response includes all 28 ingredients (including unmapped ingredients) and two retailers, Dunnes Stores and Tesco Ireland. IDs, aliases, products, retailer website URLs, and provider configuration are omitted. `null` retailer means all supported retailers.

`PreferenceRequest` requires all fields except `retailerCode`. `PreferenceValidator.validate` combines Jakarta constraints with allowed time choices and catalog lookups. The T06 suggestion service calls it before filtering recipes. Exact lowercase enum codes are required; blank, unknown, missing, and unsupported values are rejected without substitution. Servings must be 1–8. Exclusions must be a collection, may be empty, and are deduplicated in selection order. Each exclusion must be a known ingredient code; an optional retailer must be a known code. Fixed enum choices, bounds, and cooking times are shared with the options service. `any` is a preference sentinel; recipes use a concrete cuisine.

Exclusions mean ingredients unwanted in recipes. They are separate from the later already-owned grocery checklist and do not guarantee allergy protection. T05 will filter the downloaded ingredient list by name/form as visitors type, confirm selections, and show removable tags. Free text, alias search, and server-side search are deferred.

`RecipeIngredient` contains an ingredient code, a required positive decimal quantity, and `QuantityUnit` (`G`, `KG`, `ML`, `L`, `EACH`). Use Jakarta `Validator` directly or `@Valid` on enclosing fields for nested validation. Persistence `PackUnit` is unchanged. `POST /api/v1/meal-suggestions` accepts meal preferences (cuisine, meal type, servings, cooking-time limit, diet, and exclusions) and returns `{generationMode,message,recipes}`. It filters the Flyway-seeded sample recipes by meal, cuisine (`any` matches all), cooking time, diet, and exclusions. Retailer selection is deferred until a recipe is selected for shopping, where product mappings can be applied to that recipe's ingredients. The endpoint returns up to three recipes in stable code order, with catalog-backed ingredients scaled to requested servings using decimal arithmetic, plus numbered steps. Empty matches include an explanatory message. Every response uses `generationMode: "sample"`; no AI provider is called.

API errors contain only `code`, a safe `message`, a fresh server-generated UUID `requestId`, and `fieldErrors` (`field`/`message` pairs):

| Status | Code | Meaning |
| --- | --- | --- |
| 400 | `INVALID_PREFERENCES` | Invalid preferences or malformed request input |
| 503 | `DATABASE_UNAVAILABLE` | Catalog query or database transaction acquisition failed |
| 500 | `INTERNAL_ERROR` | Unexpected application failure |
| MVC status (e.g. 404, 405, 406, 415) | `REQUEST_ERROR` | Framework request error; status and applicable headers are preserved |

Errors never include rejected values, database/exception details, stack traces, or credentials. Field errors may be empty for malformed input and general failures. The health endpoint retains its separate `UP`/`DOWN` contract.

Tests cover preferences validation and the HTTP suggestion contract against migrated Testcontainers PostgreSQL, including filters, diet compatibility, exclusions, `any`, scaling, stable limits/order, and empty results.

## Test and Package

From this directory:

```sh
./gradlew test
./gradlew clean build
```

Docker must be running for integration tests. Testcontainers creates disposable PostgreSQL databases and random test credentials; it does not use your Compose database or require your `.env`. Docker-dependent tests fail visibly if Docker is unavailable. No H2 substitution or silent skipping is configured.

`test` runs the JUnit tests. `clean build` removes previous output, runs checks, and packages the application. Test results appear in `build/reports/tests/test/index.html`. To run the executable Spring Boot artifact:

```sh
java -jar build/libs/meal-planner-0.0.1-SNAPSHOT.jar
```

Direct `java -jar` launches require `APP_DB_PASSWORD` (and any custom `DATABASE_URL`) in the backend environment; automatic root `.env` loading is specific to the development `bootRun` command.

The `-plain.jar` artifact, when present, is not the executable Spring Boot package. On Windows, use `gradlew.bat` in place of `./gradlew`. The Windows wrapper is included but has not been executed on Windows.

## How the Pieces Fit Together

- `build.gradle` applies the Java and Spring Boot plugins, selects Java 21, declares dependencies, and enables JUnit Platform for tests. The Boot plugin adds tasks such as `bootRun` and `bootJar`.
- `settings.gradle` sets the project name, which contributes to the packaged artifact's filename.
- `MealPlannerApplication` is the entry point. `@SpringBootApplication` enables Spring Boot configuration and scans the `ie.mealplanner` package and its children.
- `HealthController` is a Spring-managed HTTP controller. `@GetMapping` connects the URL to its method, and Spring serializes the response to JSON.
- `HealthResponse` is a small Java record defining the response fields. Records provide immutable data carriers with value-based equality.
- `HealthEndpointTest` starts the application on a random port against real PostgreSQL and checks the HTTP health contract. `HealthUnavailableTest` verifies a sanitized 503 response when connection acquisition fails.
- `catalog` contains internal JPA entities and repositories for ingredients, aliases, retailers, products, and reviewed mappings. `ProductRepository.findMatches` accepts a stable ingredient code and an optional retailer code; its query uses bound parameters.
- `V1__create_catalog.sql` creates the schema and integrity constraints. Its original read-only grants remain unchanged for Flyway checksum compatibility; the application now owns the tables it creates, so those historical grants do not restrict its writes. `V2__seed_initial_catalog.sql` inserts the reviewed catalog. Flyway runs before Hibernate schema validation; Hibernate never creates or updates tables.
- `CatalogPersistenceTest` checks real migrations, repository reads, invalid data rejection, rerun behavior, and application-owner permissions, including successful writes without superuser privileges. Fictional invalid records occur only in tests.

The endpoint test is an integration test: it verifies several components working together. A unit test would exercise an isolated calculation or business rule. The health checks cover both real database access and the error response rather than repeating a constant controller return value.

## Wrapper Provenance

The scripts and wrapper JAR were generated using Gradle's official generator from a cached Gradle 8.14.3 distribution. The JAR matches the [published wrapper checksum](https://gradle.org/release-checksums/), and the properties pin the binary distribution's published SHA-256 checksum. License headers are retained.

Keep `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` together in version control. The [Gradle Wrapper guide](https://docs.gradle.org/current/userguide/gradle_wrapper.html) explains their roles.

## Inspect and Preserve Catalog Data

From the repository root, open PostgreSQL's SQL prompt:

```sh
docker compose exec postgres sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Useful read-only queries:

```sql
SELECT count(*) FROM ingredient;                  -- 28
SELECT count(*) FROM recipe;                      -- 12
SELECT count(*) FROM product;                     -- 12
SELECT count(*) FROM ingredient_product_mapping;  -- 12
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT i.code, r.code AS retailer, p.name, p.pack_label, p.verified_on
FROM ingredient_product_mapping m
JOIN ingredient i ON i.id = m.ingredient_id
JOIN product p ON p.id = m.product_id
JOIN retailer r ON r.id = p.retailer_id
ORDER BY i.code, r.code;
```

Exit with `\q`. Use `docker compose stop` and `docker compose up -d --wait` for a non-destructive persistence check. `docker compose down` also retains the named volume. Restart the backend and confirm the counts remain unchanged and migrations do not reinsert data.

**Destructive reset:** `docker compose down --volumes` permanently deletes this Compose project's database volume. Run it only when you deliberately intend to discard all local database data. An agent must obtain explicit approval before doing so. Start PostgreSQL and the backend again to initialize an empty database.

## Security Boundaries

Flyway migrations and normal backend queries both use `meal_planner_app` and the single `APP_DB_PASSWORD` value. This non-superuser owns the application database and tables, so it can read, insert, update, delete, and change its schema. It cannot create other databases or roles, bypass row-level security, or act as a replication/superuser account.

The Docker bootstrap administrator (`POSTGRES_USER`, default `meal_planner_owner`) initializes PostgreSQL and creates the application owner. Its separate `POSTGRES_PASSWORD` is a Docker setup credential, not a backend credential; the `bootRun` task removes it from the backend environment. Both secrets are still in the private root `.env` because Docker needs them for a fresh volume. This local file is not a strong isolation boundary against arbitrary code running as your OS user. Keep access to Docker restricted as well: Docker administrators can access container configuration and data. Separate production migration execution and secret storage remain T16 considerations.

The HTTP endpoints include health, options, and sample meal suggestions. Catalog entities are not exposed as API responses. AI rate limits and spending bounds remain required before live generation in T13. There are no AI keys, paid API calls, AI tools, or retailer purchases in T06. Treat future model responses and external page content as untrusted data; product verification does not authorize executing content from retailer pages.

Before review/commit, check changed files for secrets and ensure only placeholder configuration is tracked. Avoid shell tracing, full environment dumps, `docker inspect` output containing credentials, and unredacted Compose configuration. Frontend dependency auditing uses `npm audit`; no permanent scanning service or new security package has been introduced.

## Verification Status

See the [development plan](../DEVELOPMENT_PLAN.md) for actual T03/T04 results, including any remaining blockers. Earlier T01 sandbox failures remain historical records there; they do not describe the later successful JDK 21/Testcontainers runs.
