package ie.mealplanner.suggestions;

import ie.mealplanner.preferences.PreferenceRequest;
import ie.mealplanner.preferences.PreferenceValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealSuggestionsService {
    private final RecipeRepository recipes;
    private final PreferenceValidator validator;

    public MealSuggestionsService(RecipeRepository recipes, PreferenceValidator validator) {
        this.recipes = recipes;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public MealSuggestionsResponse suggest(MealSuggestionsRequest unvalidated) {
        PreferenceRequest request = validator.validate(unvalidated.toPreferenceRequest());
        var matches = request.excludedIngredientCodes().isEmpty()
                ? recipes.findCandidates(request.mealType().code(), request.cuisine().code(),
                        request.maxCookingTimeMinutes(), request.diet().code())
                : recipes.findCandidatesExcluding(request.mealType().code(), request.cuisine().code(),
                        request.maxCookingTimeMinutes(), request.diet().code(), request.excludedIngredientCodes());
        List<MealSuggestionsResponse.Recipe> recipeDtos = matches.stream()
                .map(recipe -> toDto(recipe, request.servings())).toList();
        String message = recipeDtos.isEmpty()
                ? "No sample meals match these preferences. Try a longer cooking time or remove an exclusion."
                : "Here are sample meal ideas that match your preferences.";
        return new MealSuggestionsResponse("sample", message, recipeDtos);
    }

    private MealSuggestionsResponse.Recipe toDto(RecipeRepository.RecipeSummary row, int servings) {
        BigDecimal scale = BigDecimal.valueOf(servings).divide(BigDecimal.valueOf(row.getServings()), 8, java.math.RoundingMode.HALF_UP);
        var recipeIngredients = recipes.findIngredients(row.getId()).stream()
                .map(ingredient -> new MealSuggestionsResponse.Ingredient(ingredient.getCode(), ingredient.getName(), ingredient.getForm(),
                        ingredient.getQuantity().multiply(scale).stripTrailingZeros(), ingredient.getUnit()))
                .toList();
        var recipeSteps = recipes.findSteps(row.getId()).stream()
                .map(step -> new MealSuggestionsResponse.Step(step.getNumber(), step.getInstruction()))
                .toList();
        List<String> labels = new ArrayList<>();
        if (row.getVegetarian()) labels.add("vegetarian");
        if (row.getVegan()) labels.add("vegan");
        return new MealSuggestionsResponse.Recipe(row.getCode(), row.getName(), row.getDescription(), row.getCuisine(), row.getMealType(),
                servings, row.getCookingTimeMinutes(), labels, recipeIngredients, recipeSteps);
    }
}
