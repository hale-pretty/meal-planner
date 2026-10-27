package ie.mealplanner.preferences;

import ie.mealplanner.api.FieldError;
import ie.mealplanner.catalog.IngredientRepository;
import ie.mealplanner.catalog.RetailerRepository;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceValidator {
    private final Validator validator;
    private final IngredientRepository ingredients;
    private final RetailerRepository retailers;

    public PreferenceValidator(Validator validator, IngredientRepository ingredients, RetailerRepository retailers) {
        this.validator = validator;
        this.ingredients = ingredients;
        this.retailers = retailers;
    }

    /** T06 should call this before invoking any suggestion provider. */
    @Transactional(readOnly = true)
    public PreferenceRequest validate(PreferenceRequest request) {
        if (request == null) {
            throw new InvalidPreferencesException(List.of(new FieldError("preferences", "Provide preferences")));
        }
        List<FieldError> errors = new ArrayList<>();
        validator.validate(request).forEach(error -> errors.add(
                new FieldError(error.getPropertyPath().toString(), error.getMessage())));
        if (request.maxCookingTimeMinutes() != null && !PreferenceChoices.COOKING_TIMES.contains(request.maxCookingTimeMinutes())) {
            errors.add(new FieldError("maxCookingTimeMinutes", "Choose a supported cooking time"));
        }
        if (!errors.isEmpty()) { throw invalid(errors); }
        // One bound IN query for exclusions, including ingredients without product mappings.
        var knownCodes = ingredients.findAllByCodeIn(request.excludedIngredientCodes()).stream()
                .map(ingredient -> ingredient.getCode()).collect(java.util.stream.Collectors.toSet());
        if (!knownCodes.containsAll(request.excludedIngredientCodes())) {
            errors.add(new FieldError("excludedIngredientCodes", "Choose known ingredient codes"));
        }
        if (request.retailerCode() != null && (request.retailerCode().isBlank()
                || retailers.findByCode(request.retailerCode()).isEmpty())) {
            errors.add(new FieldError("retailerCode", "Choose a supported retailer"));
        }
        if (!errors.isEmpty()) { throw invalid(errors); }
        return request;
    }

    private InvalidPreferencesException invalid(List<FieldError> errors) {
        errors.sort(Comparator.comparing(FieldError::field).thenComparing(FieldError::message));
        return new InvalidPreferencesException(errors);
    }
}
