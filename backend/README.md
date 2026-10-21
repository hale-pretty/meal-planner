# Backend — T01

This is the first backend increment: a Spring Boot application with a health endpoint and an HTTP integration test. It starts independently of the future catalog database and AI integration.

## Toolchain

| Tool | Project version | Where configured |
| --- | --- | --- |
| JDK | 21 | Java toolchain in `build.gradle` |
| Spring Boot | 3.5.16 | Plugin in `build.gradle` |
| Gradle | 8.14.3 | `gradle/wrapper/gradle-wrapper.properties` |

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

## Run and Check the Endpoint

From the repository root:

```sh
cd backend
./gradlew bootRun
```

Spring Boot uses port 8080 by default. In a second terminal:

```sh
curl --fail-with-body -i http://localhost:8080/api/v1/health
```

Expected: HTTP 200, a JSON content type, and this body:

```json
{"status":"UP"}
```

This endpoint confirms that the application can serve requests. It does not claim to check future database or AI-provider connectivity. Stop the server with Ctrl+C.

## Test and Package

From this directory:

```sh
./gradlew test
./gradlew clean build
```

`test` runs the JUnit tests. `clean build` removes previous output, runs checks, and packages the application. Test results appear in `build/reports/tests/test/index.html`. To run the executable Spring Boot artifact:

```sh
java -jar build/libs/meal-planner-0.0.1-SNAPSHOT.jar
```

The `-plain.jar` artifact, when present, is not the executable Spring Boot package. On Windows, use `gradlew.bat` in place of `./gradlew`. The Windows wrapper is included but has not been executed on Windows.

## How the Pieces Fit Together

- `build.gradle` applies the Java and Spring Boot plugins, selects Java 21, declares dependencies, and enables JUnit Platform for tests. The Boot plugin adds tasks such as `bootRun` and `bootJar`.
- `settings.gradle` sets the project name, which contributes to the packaged artifact's filename.
- `MealPlannerApplication` is the entry point. `@SpringBootApplication` enables Spring Boot configuration and scans the `ie.mealplanner` package and its children.
- `HealthController` is a Spring-managed HTTP controller. `@GetMapping` connects the URL to its method, and Spring serializes the response to JSON.
- `HealthResponse` is a small Java record defining the response fields. Records provide immutable data carriers with value-based equality.
- `HealthEndpointTest` starts the application on a random port and sends a real HTTP request using `TestRestTemplate`. It checks status, content type, and response data.

The endpoint test is an integration test: it verifies several components working together. A unit test would exercise an isolated calculation or business rule. The constant health response does not warrant a separate unit test that simply repeats the controller's implementation.

## Wrapper Provenance

The scripts and wrapper JAR were generated using Gradle's official generator from a cached Gradle 8.14.3 distribution. The JAR matches the [published wrapper checksum](https://gradle.org/release-checksums/), and the properties pin the binary distribution's published SHA-256 checksum. License headers are retained.

Keep `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` together in version control. The [Gradle Wrapper guide](https://docs.gradle.org/current/userguide/gradle_wrapper.html) explains their roles.

## Verification Status

The generated wrapper reports Gradle 8.14.3 using the cached distribution and an isolated writable cache. Its JAR checksum and Unix shell syntax have been verified. The agent environment needs native-service extraction disabled for this limited wrapper check; this is an environment-specific verification flag, not a project setting.

The project build has not been verified: the agent environment blocks Gradle's local file-lock communication sockets with `Operation not permitted`. It also currently exposes JDK 17 and restricted dependency-download access. These infrastructure failures are not JUnit results.

Before marking T01 ready for review, run the version checks, `./gradlew clean build`, and the manual HTTP check above with JDK 21 in a normal development terminal. Record the results in the [development plan](../DEVELOPMENT_PLAN.md).
