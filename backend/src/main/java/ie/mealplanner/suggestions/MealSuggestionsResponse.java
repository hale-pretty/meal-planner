package ie.mealplanner.suggestions;

import java.math.BigDecimal;
import java.util.List;

public record MealSuggestionsResponse(String generationMode, String message, List<Recipe> recipes) {
    public record Recipe(String code, String name, String description, String cuisine, String mealType,
            int servings, int cookingTimeMinutes, List<String> dietLabels, List<Ingredient> ingredients,
            List<Step> steps) { }
    public record Ingredient(String code, String name, String form, BigDecimal quantity, String unit) { }
    public record Step(int number, String instruction) { }
}
