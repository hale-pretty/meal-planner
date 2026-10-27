package ie.mealplanner.preferences;

import java.util.List;

public final class PreferenceChoices {
    public static final int MIN_SERVINGS = 1;
    public static final int MAX_SERVINGS = 8;
    public static final List<Integer> COOKING_TIMES = List.of(15, 30, 45, 60);

    private PreferenceChoices() { }

    public static PreferenceRequest defaults() {
        return new PreferenceRequest(Cuisine.ANY, MealType.DINNER, 2, 30, Diet.NONE, List.of(), null);
    }
}
