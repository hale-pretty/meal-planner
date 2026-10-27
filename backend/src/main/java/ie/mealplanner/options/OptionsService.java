package ie.mealplanner.options;

import ie.mealplanner.catalog.IngredientRepository;
import ie.mealplanner.catalog.RetailerRepository;
import ie.mealplanner.preferences.*;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptionsService {
    private final IngredientRepository ingredients;
    private final RetailerRepository retailers;

    public OptionsService(IngredientRepository ingredients, RetailerRepository retailers) {
        this.ingredients = ingredients;
        this.retailers = retailers;
    }

    @Transactional(readOnly = true)
    public OptionsResponse options() {
        return new OptionsResponse(
                Arrays.stream(Cuisine.values()).map(v -> new OptionsResponse.Choice(v.code(), v.label())).toList(),
                Arrays.stream(MealType.values()).map(v -> new OptionsResponse.Choice(v.code(), v.label())).toList(),
                Arrays.stream(Diet.values()).map(v -> new OptionsResponse.Choice(v.code(), v.label())).toList(),
                new OptionsResponse.Servings(PreferenceChoices.MIN_SERVINGS, PreferenceChoices.MAX_SERVINGS),
                PreferenceChoices.COOKING_TIMES,
                ingredients.findAllByOrderByNameAscCodeAsc().stream()
                        .map(v -> new OptionsResponse.IngredientOption(v.getCode(), v.getName(), v.getForm())).toList(),
                retailers.findAllByOrderByNameAscCodeAsc().stream()
                        .map(v -> new OptionsResponse.RetailerOption(v.getCode(), v.getName())).toList(),
                "sample", PreferenceChoices.defaults());
    }
}
