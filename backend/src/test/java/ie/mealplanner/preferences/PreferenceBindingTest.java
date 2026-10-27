package ie.mealplanner.preferences;

import ie.mealplanner.api.ApiExceptionHandler;
import ie.mealplanner.catalog.IngredientRepository;
import ie.mealplanner.catalog.RetailerRepository;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** A standalone test fixture exercises future request binding without adding a production endpoint. */
class PreferenceBindingTest {
    @RestController
    static class BindingFixture {
        private final PreferenceValidator validator;
        BindingFixture(PreferenceValidator validator) { this.validator = validator; }
        @PostMapping("/test-preferences")
        PreferenceRequest accept(@Valid @RequestBody PreferenceRequest request) { return validator.validate(request); }
    }

    private static final String VALID = """
            {"cuisine":"any","mealType":"dinner","servings":2,"maxCookingTimeMinutes":30,
             "diet":"none","excludedIngredientCodes":[],"retailerCode":null}
            """;

    @Test void validatesBoundRequestsAndNeverReflectsInvalidValues() throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var ingredients = mock(IngredientRepository.class);
            when(ingredients.findAllByCodeIn(any())).thenReturn(List.of());
            var validator = new PreferenceValidator(factory.getValidator(), ingredients, mock(RetailerRepository.class));
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new BindingFixture(validator))
                    .setControllerAdvice(new ApiExceptionHandler()).build();
            mvc.perform(post("/test-preferences").contentType(MediaType.APPLICATION_JSON).content(VALID))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.excludedIngredientCodes").isEmpty());
            for (String body : List.of("{}", "null", "{", VALID.replace("\"any\"", "\"private-invalid-value\""),
                    VALID.replace("\"dinner\"", "\"\""), VALID.replace("\"none\"", "\"private-invalid-value\""),
                    VALID.replace("[]", "null"), VALID.replace("[]", "\"private-invalid-value\""),
                    VALID.replace("[]", "[\"private-invalid-value\"]"))) {
                mvc.perform(post("/test-preferences").contentType(MediaType.APPLICATION_JSON).content(body))
                        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PREFERENCES"))
                        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-invalid-value"))));
            }
            mvc.perform(post("/test-preferences").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(jsonPath("$.fieldErrors.length()").value(6));
        }
    }
}
