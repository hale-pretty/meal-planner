package ie.mealplanner.suggestions;

import ie.mealplanner.preferences.Cuisine;
import ie.mealplanner.preferences.Diet;
import ie.mealplanner.preferences.MealType;
import ie.mealplanner.preferences.PreferenceRequest;
import java.util.List;

/** Meal discovery inputs. Retailer selection belongs to the later shopping step. */
public record MealSuggestionsRequest(Cuisine cuisine, MealType mealType, Integer servings,
        Integer maxCookingTimeMinutes, Diet diet, List<String> excludedIngredientCodes) {
    PreferenceRequest toPreferenceRequest() {
        return new PreferenceRequest(cuisine, mealType, servings, maxCookingTimeMinutes,
                diet, excludedIngredientCodes, null);
    }
}
