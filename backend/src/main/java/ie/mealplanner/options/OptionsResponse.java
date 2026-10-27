package ie.mealplanner.options;

import ie.mealplanner.preferences.PreferenceRequest;
import java.util.List;

public record OptionsResponse(List<Choice> cuisines, List<Choice> mealTypes, List<Choice> diets,
        Servings servings, List<Integer> maxCookingTimeMinutes, List<IngredientOption> ingredients,
        List<RetailerOption> retailers, String generationMode, PreferenceRequest defaults) {
    public record Choice(String code, String label) { }
    public record Servings(int min, int max) { }
    public record IngredientOption(String code, String name, String form) { }
    public record RetailerOption(String code, String name) { }
}
