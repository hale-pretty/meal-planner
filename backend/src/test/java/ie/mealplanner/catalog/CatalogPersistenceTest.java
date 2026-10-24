package ie.mealplanner.catalog;

import ie.mealplanner.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CatalogPersistenceTest extends PostgresIntegrationTest {
    @Autowired private IngredientRepository ingredients;
    @Autowired private IngredientAliasRepository aliases;
    @Autowired private RetailerRepository retailers;
    @Autowired private ProductRepository products;
    @Autowired private IngredientProductMappingRepository mappings;
    @Autowired private Flyway flyway;
    @Autowired private JdbcTemplate applicationDatabase;

    @Test
    void freshMigrationsCreateTheReviewedCatalog() {
        assertThat(ingredients.count()).isEqualTo(20);
        assertThat(aliases.count()).isEqualTo(10);
        assertThat(retailers.count()).isEqualTo(2);
        assertThat(products.count()).isEqualTo(12);
        assertThat(mappings.count()).isEqualTo(12);
        assertThat(flyway.info().applied()).hasSize(2);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(ingredients.findByCode("tomato-fresh").orElseThrow().getId())
                .isNotEqualTo(ingredients.findByCode("tomato-canned").orElseThrow().getId());
        assertThat(aliases.findAllByIngredientCode("beef-mince-raw"))
                .extracting(IngredientAlias::getName).containsExactly("ground beef");
        assertThat(retailers.findByCode("tesco")).isPresent();
        assertThat(retailers.findByCode("dunnes")).isPresent();
    }

    @Test
    void productLookupUsesIngredientIdentityAndOptionalRetailer() {
        assertThat(products.findMatches("white-rice-dry", null)).hasSize(2);
        assertThat(products.findMatches("white-rice-dry", "tesco"))
                .singleElement().satisfies(product -> {
                    assertThat(product.getRetailer().getCode()).isEqualTo("tesco");
                    assertThat(product.getPackQuantity()).isEqualByComparingTo(new BigDecimal("1000"));
                    assertThat(product.getPackUnit()).isEqualTo(PackUnit.G);
                    assertThat(product.getVerifiedOn()).isEqualTo(LocalDate.of(2026, 10, 22));
                });
        assertThat(products.findMatches("white-rice-dry", "dunnes")).hasSize(1);
        assertThat(products.findMatches("white-rice-dry", "unknown")).isEmpty();
        assertThat(products.findMatches("white-rice-dry' OR '1'='1", null)).isEmpty();
        assertThat(products.findMatches("tomato-canned", null)).hasSize(2);
        assertThat(products.findMatches("tomato-fresh", null)).isEmpty();
        assertThat(products.findMatches("fish-sauce", null)).isEmpty();
    }

    @Test
    @Transactional(readOnly = true)
    void mappingsPreserveRelationshipsAndUnknownPackQuantities() {
        assertThat(mappings.findAllByIngredientCode("milk-whole"))
                .hasSize(2).allSatisfy(mapping -> {
                    assertThat(mapping.getIngredient().getCode()).isEqualTo("milk-whole");
                    assertThat(mapping.getProduct().getPackUnit()).isEqualTo(PackUnit.ML);
                    assertThat(mapping.getProduct().getPackQuantity()).isEqualByComparingTo("1000");
                    assertThat(mapping.getReviewedOn()).isEqualTo(LocalDate.of(2026, 10, 22));
                });
        assertThat(products.findMatches("onion-fresh", "tesco")).singleElement().satisfies(product -> {
            assertThat(product.getPackQuantity()).isNull();
            assertThat(product.getPackUnit()).isNull();
            assertThat(product.getPackLabel()).contains("Loose");
        });
    }

    @Test
    void migratingAnExistingDatabaseDoesNotDuplicateSeedRows() {
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(ingredients.count()).isEqualTo(20);
        assertThat(products.count()).isEqualTo(12);
        assertThat(mappings.count()).isEqualTo(12);
    }

    static Stream<String> invalidCatalogWrites() {
        String product = "INSERT INTO product (retailer_id, name, product_url, pack_label, pack_quantity, pack_unit, verified_on) ";
        String suffix = " FROM product LIMIT 1";
        return Stream.of(
                "INSERT INTO ingredient(code, name, form) VALUES ('white-rice-dry', 'Duplicate', 'dry')",
                "INSERT INTO ingredient(code, name, form) VALUES ('fixture', ' ', 'dry')",
                "INSERT INTO ingredient(code, name, form) VALUES ('fixture', 'Fixture', NULL)",
                "INSERT INTO retailer(code, name, website_url) VALUES ('tesco', 'Duplicate', 'https://fixture.invalid')",
                "INSERT INTO ingredient_alias(ingredient_id, name) SELECT ingredient_id, name FROM ingredient_alias LIMIT 1",
                "INSERT INTO ingredient_alias(ingredient_id, name) VALUES (-1, 'Orphan')",
                "INSERT INTO ingredient_product_mapping(ingredient_id, product_id, reviewed_on) SELECT ingredient_id, product_id, reviewed_on FROM ingredient_product_mapping LIMIT 1",
                "INSERT INTO ingredient_product_mapping(ingredient_id, product_id, reviewed_on) VALUES (-1, -1, CURRENT_DATE)",
                "INSERT INTO ingredient_product_mapping(ingredient_id, product_id, reviewed_on) SELECT ingredient_id, product_id, NULL FROM ingredient_product_mapping LIMIT 1",
                product + "SELECT retailer_id, name, product_url, pack_label, pack_quantity, pack_unit, verified_on" + suffix,
                product + "SELECT -1, 'Fixture', 'https://fixture.invalid/item', 'Fixture', 1, 'G', CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'https://fixture.invalid/item', 'Fixture', -1, 'G', CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'https://fixture.invalid/item', 'Fixture', 0, 'G', CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'https://fixture.invalid/item', 'Fixture', 1, NULL, CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'https://fixture.invalid/item', 'Fixture', NULL, 'G', CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'https://fixture.invalid/item', 'Fixture', 1, 'KG', CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'http://fixture.invalid/item', 'Fixture', 1, 'G', CURRENT_DATE" + suffix,
                product + "SELECT retailer_id, 'Fixture', 'https://fixture.invalid/item', 'Fixture', 1, 'G', NULL" + suffix
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCatalogWrites")
    void databaseRejectsInvalidCatalogData(String sql) {
        assertThatThrownBy(() -> applicationDatabase.execute(sql)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void applicationOwnerCanPersistUpdateAndDeleteThroughJpa() {
        Ingredient fixture = ingredients.saveAndFlush(new Ingredient("write-test-only", "Fixture", "dry"));
        Long id = fixture.getId();
        assertThat(id).isNotNull();
        assertThat(ingredients.findByCode("write-test-only")).isPresent();
        assertThat(applicationDatabase.update("UPDATE ingredient SET name = ? WHERE id = ?", "Updated fixture", id)).isEqualTo(1);
        assertThat(applicationDatabase.queryForObject("SELECT name FROM ingredient WHERE id = ?", String.class, id))
                .isEqualTo("Updated fixture");
        ingredients.deleteById(id);
        ingredients.flush();
        assertThat(ingredients.findByCode("write-test-only")).isEmpty();
    }

    @Test
    @Transactional
    void migrationsAndQueriesUseTheSameNonSuperuserOwner() {
        assertThat(applicationDatabase.queryForObject("SELECT current_user", String.class)).isEqualTo("meal_planner_app");
        JdbcTemplate migrationDatabase = new JdbcTemplate(flyway.getConfiguration().getDataSource());
        assertThat(migrationDatabase.queryForObject("SELECT current_user", String.class)).isEqualTo("meal_planner_app");
        assertThat(applicationDatabase.queryForObject("""
                SELECT NOT (rolsuper OR rolcreatedb OR rolcreaterole OR rolreplication OR rolbypassrls)
                FROM pg_roles WHERE rolname = current_user
                """, Boolean.class)).isTrue();
        assertThat(applicationDatabase.queryForObject("""
                SELECT pg_get_userbyid(datdba) FROM pg_database WHERE datname = current_database()
                """, String.class)).isEqualTo("meal_planner_app");
        assertThat(applicationDatabase.queryForList("""
                SELECT tableowner FROM pg_tables WHERE schemaname = 'public'
                """, String.class)).isNotEmpty().containsOnly("meal_planner_app");
        applicationDatabase.execute("CREATE TABLE migration_permission_fixture(id BIGINT PRIMARY KEY)");
        applicationDatabase.execute("ALTER TABLE migration_permission_fixture ADD COLUMN name TEXT");
        applicationDatabase.execute("DROP TABLE migration_permission_fixture");
    }
}
