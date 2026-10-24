package ie.mealplanner.catalog;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class CatalogOwnershipUpgradeTest {
    private static final String APP_PASSWORD = UUID.randomUUID().toString();

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11")
            .withDatabaseName("meal_planner")
            .withUsername("meal_planner_owner")
            .withPassword(UUID.randomUUID().toString())
            .withEnv("APP_DB_PASSWORD", APP_PASSWORD)
            .withCopyFileToContainer(MountableFile.forClasspathResource("database/legacy-two-account.sql"),
                    "/docker-entrypoint-initdb.d/10-legacy.sql")
            .withCopyFileToContainer(MountableFile.forClasspathResource("database/upgrade-single-account.sql"),
                    "/tmp/upgrade-single-account.sql");

    @Test
    void existingCatalogCanChangeOwnersWithoutDataLossOrMigrationRepair() throws Exception {
        Flyway oldMigrations = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()).load();
        oldMigrations.migrate();
        JdbcTemplate oldOwner = new JdbcTemplate(oldMigrations.getConfiguration().getDataSource());
        oldOwner.update("INSERT INTO ingredient(code,name,form) VALUES ('existing-custom-ingredient','User catalog entry','fresh')");
        oldOwner.execute("CREATE TABLE unrelated_fixture(id BIGINT)");

        JdbcTemplate application = new JdbcTemplate(new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), "meal_planner_app", APP_PASSWORD));
        assertThatThrownBy(() -> application.update("UPDATE ingredient SET name = name"))
                .isInstanceOf(DataAccessException.class);
        String before = fingerprint(oldOwner);
        var result = POSTGRES.execInContainer("psql", "-U", POSTGRES.getUsername(),
                "-d", POSTGRES.getDatabaseName(), "-f", "/tmp/upgrade-single-account.sql");
        assertThat(result.getExitCode()).isZero();
        assertThat(fingerprint(application)).isEqualTo(before);
        assertThat(application.queryForObject("SELECT pg_get_userbyid(datdba) FROM pg_database WHERE datname = current_database()", String.class))
                .isEqualTo("meal_planner_app");
        assertThat(application.queryForObject("SELECT tableowner FROM pg_tables WHERE tablename = 'unrelated_fixture' AND schemaname = 'public'", String.class))
                .isEqualTo(POSTGRES.getUsername());

        Flyway newMigrations = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), "meal_planner_app", APP_PASSWORD).load();
        assertThat(newMigrations.validateWithResult().validationSuccessful).isTrue();
        assertThat(newMigrations.migrate().migrationsExecuted).isZero();
        assertThat(application.update("UPDATE ingredient SET name = ? WHERE code = ?", "Updated user entry", "existing-custom-ingredient"))
                .isEqualTo(1);
        assertThat(application.update("INSERT INTO ingredient(code,name,form) VALUES ('after-upgrade','New entry','dry')"))
                .isEqualTo(1);
        assertThat(application.update("DELETE FROM ingredient WHERE code = 'after-upgrade'"))
                .isEqualTo(1);
        application.execute("CREATE TABLE migration_check(id BIGINT)");
        application.execute("DROP TABLE migration_check");
        assertThat(application.queryForObject("SELECT NOT (rolsuper OR rolcreatedb OR rolcreaterole OR rolreplication OR rolbypassrls) FROM pg_roles WHERE rolname=current_user", Boolean.class))
                .isTrue();
        assertThat(POSTGRES.execInContainer("psql", "-U", POSTGRES.getUsername(), "-d", POSTGRES.getDatabaseName(),
                "-f", "/tmp/upgrade-single-account.sql").getExitCode()).isZero();
    }

    private static String fingerprint(JdbcTemplate database) {
        StringBuilder result = new StringBuilder();
        for (String table : new String[] {"ingredient", "ingredient_alias", "retailer", "product", "ingredient_product_mapping", "flyway_schema_history"}) {
            // Identifiers are fixed test constants, never user-supplied input.
            result.append(database.queryForObject("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY to_jsonb(t)::text)::text, '[]') FROM public."
                    + table + " t", String.class));
        }
        return result.toString();
    }
}
