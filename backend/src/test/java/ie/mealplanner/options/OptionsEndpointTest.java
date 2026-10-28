package ie.mealplanner.options;

import com.fasterxml.jackson.databind.JsonNode;
import ie.mealplanner.PostgresIntegrationTest;
import ie.mealplanner.preferences.PreferenceValidator;
import ie.mealplanner.preferences.PreferenceRequest;
import ie.mealplanner.preferences.Cuisine;
import ie.mealplanner.preferences.MealType;
import ie.mealplanner.preferences.Diet;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OptionsEndpointTest extends PostgresIntegrationTest {
    @Autowired private TestRestTemplate http;
    @Autowired private PreferenceValidator validator;

    @Test void returnsFixedOptionsDefaultsAndOnlySafeCatalogFieldsInStableOrder() throws Exception {
        var response = http.getForEntity("/api/v1/options", JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertFields(body, "cuisines", "mealTypes", "diets", "servings", "maxCookingTimeMinutes", "ingredients", "retailers", "generationMode", "defaults");
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        assertThat(body.get("cuisines")).isEqualTo(mapper.readTree("""
                [{"code":"any","label":"Any cuisine"},{"code":"american","label":"American"},{"code":"irish","label":"Irish"},{"code":"vietnamese","label":"Vietnamese"}]
                """));
        assertThat(body.get("mealTypes")).isEqualTo(mapper.readTree("""
                [{"code":"breakfast","label":"Breakfast"},{"code":"lunch","label":"Lunch"},{"code":"dinner","label":"Dinner"}]
                """));
        assertThat(body.get("diets")).isEqualTo(mapper.readTree("""
                [{"code":"none","label":"None"},{"code":"vegetarian","label":"Vegetarian"},{"code":"vegan","label":"Vegan"}]
                """));
        assertThat(body.get("servings")).isEqualTo(mapper.readTree("{\"min\":1,\"max\":8}"));
        assertThat(body.get("maxCookingTimeMinutes")).isEqualTo(mapper.readTree("[15,30,45,60]"));
        assertThat(body.get("generationMode").asText()).isEqualTo("sample");
        assertThat(body.get("defaults")).isEqualTo(mapper.readTree("""
                {"cuisine":"any","mealType":"dinner","servings":2,"maxCookingTimeMinutes":30,"diet":"none","excludedIngredientCodes":[],"retailerCode":null}
                """));
        assertThat(body.get("ingredients").size()).isEqualTo(28);
        assertThat(body.get("retailers").size()).isEqualTo(2);
        body.get("ingredients").forEach(item -> assertFields(item, "code", "name", "form"));
        body.get("retailers").forEach(item -> assertFields(item, "code", "name"));
        for (String field : List.of("ingredients", "retailers")) {
            List<JsonNode> entries = new ArrayList<>();
            body.get(field).forEach(entries::add);
            assertThat(entries).isSortedAccordingTo(Comparator.comparing((JsonNode n) -> n.get("name").asText())
                    .thenComparing(n -> n.get("code").asText()));
        }
        assertThat(body.get("ingredients").findValuesAsText("code")).contains("fish-sauce", "tomato-fresh");
        assertThat(body.get("retailers").findValuesAsText("code")).containsExactlyInAnyOrder("tesco", "dunnes");
        assertThat(http.getForObject("/api/v1/options", JsonNode.class)).isEqualTo(body);
        // Real repository validation includes unmapped ingredients, independent of product coverage.
        validator.validate(new PreferenceRequest(Cuisine.ANY, MealType.DINNER, 2, 30, Diet.NONE, List.of("fish-sauce"), "tesco"));
        assertThatThrownBy(() -> validator.validate(new PreferenceRequest(Cuisine.ANY, MealType.DINNER, 2, 30, Diet.NONE, List.of("unknown"), null)))
                .isInstanceOf(ie.mealplanner.preferences.InvalidPreferencesException.class);
    }

    private static void assertFields(JsonNode node, String... expected) {
        List<String> fields = new ArrayList<>();
        node.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder(expected);
    }
}
