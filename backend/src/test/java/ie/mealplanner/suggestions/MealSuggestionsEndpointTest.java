package ie.mealplanner.suggestions;

import com.fasterxml.jackson.databind.JsonNode;
import ie.mealplanner.PostgresIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MealSuggestionsEndpointTest extends PostgresIntegrationTest {
    @Autowired TestRestTemplate http;

    private JsonNode request(String cuisine, String meal, int servings, int time, String diet, List<String> excluded) {
        var body = new java.util.LinkedHashMap<String,Object>();
        body.put("cuisine", cuisine); body.put("mealType", meal); body.put("servings", servings);
        body.put("maxCookingTimeMinutes", time); body.put("diet", diet); body.put("excludedIngredientCodes", excluded);
        return http.postForObject("/api/v1/meal-suggestions", body, JsonNode.class);
    }

    @Test void filtersOrdersLimitsScalesAndReturnsRecipeContract() {
        JsonNode body=request("any","dinner",4,45,"vegan",List.of());
        assertThat(body.get("generationMode").asText()).isEqualTo("sample");
        assertThat(body.get("recipes").size()).isEqualTo(2);
        JsonNode recipe=null;
        for (JsonNode candidate : body.get("recipes")) if (candidate.get("code").asText().equals("vietnamese-coconut-chickpeas")) recipe=candidate;
        assertThat(recipe).isNotNull();
        assertThat(recipe.get("code").asText()).isEqualTo("vietnamese-coconut-chickpeas");
        assertThat(recipe.get("servings").asInt()).isEqualTo(4);
        assertThat(recipe.get("ingredients").get(0).get("quantity").decimalValue()).isEqualByComparingTo("480");
        assertThat(recipe.get("steps").get(0).get("number").asInt()).isEqualTo(1);
        assertThat(request("irish","dinner",2,45,"none",List.of()).get("recipes").get(0).get("cuisine").asText()).isEqualTo("irish");
        assertThat(request("any","breakfast",2,30,"vegetarian",List.of()).get("recipes").size()).isEqualTo(3);
        assertThat(request("any","dinner",2,15,"none",List.of()).get("recipes").size()).isZero();
        assertThat(request("any","dinner",2,60,"none",List.of("potato-fresh")).get("recipes").size()).isEqualTo(3);
        JsonNode capped=request("any","dinner",2,60,"none",List.of());
        assertThat(capped.get("recipes").size()).isEqualTo(3);
        assertThat(capped.get("recipes").get(0).get("code").asText()).isEqualTo("american-chicken-rice");
    }

    @Test void rejectsUnknownExclusionAndHasSafeEmptyMessage() {
        var invalid=request("any","dinner",2,30,"none",List.of("not-a-catalog-code"));
        assertThat(invalid.get("code").asText()).isEqualTo("INVALID_PREFERENCES");
        JsonNode empty=request("irish","breakfast",2,15,"vegan",List.of());
        assertThat(empty.get("generationMode").asText()).isEqualTo("sample");
        assertThat(empty.get("recipes")).isEmpty();
        assertThat(empty.get("message").asText()).contains("No sample meals match");
    }
}
