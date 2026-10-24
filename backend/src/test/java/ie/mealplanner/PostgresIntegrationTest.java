package ie.mealplanner;

import java.util.UUID;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/** Disposable PostgreSQL per test JVM, independent of Compose and local secrets. */
public abstract class PostgresIntegrationTest {
    private static final String APP_PASSWORD = UUID.randomUUID().toString();
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11")
            .withDatabaseName("meal_planner")
            .withUsername("meal_planner_owner")
            .withPassword(UUID.randomUUID().toString())
            .withEnv("APP_DB_PASSWORD", APP_PASSWORD)
            .withCopyFileToContainer(MountableFile.forClasspathResource("database/10-create-app-user.sql"),
                    "/docker-entrypoint-initdb.d/10-create-app-user.sql");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "meal_planner_app");
        registry.add("spring.datasource.password", () -> APP_PASSWORD);
    }
}
