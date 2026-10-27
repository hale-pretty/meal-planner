package ie.mealplanner.preferences;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Exclusions are unwanted ingredients; owned groceries belong to the later checklist. */
public record PreferenceRequest(
        @NotNull(message = "Choose a cuisine") Cuisine cuisine,
        @NotNull(message = "Choose a meal type") MealType mealType,
        @NotNull(message = "Enter servings") @Min(value = PreferenceChoices.MIN_SERVINGS, message = "Servings must be between 1 and 8")
        @Max(value = PreferenceChoices.MAX_SERVINGS, message = "Servings must be between 1 and 8") Integer servings,
        @NotNull(message = "Choose a cooking time") Integer maxCookingTimeMinutes,
        @NotNull(message = "Choose a diet") Diet diet,
        @NotNull(message = "Provide ingredient exclusions") List<@NotBlank(message = "Choose an ingredient code") String> excludedIngredientCodes,
        String retailerCode) {
    public PreferenceRequest {
        if (excludedIngredientCodes != null) {
            excludedIngredientCodes = Collections.unmodifiableList(new ArrayList<>(new LinkedHashSet<>(excludedIngredientCodes)));
        }
    }
}
