package ie.mealplanner.preferences;

import ie.mealplanner.api.FieldError;
import java.util.List;

public class InvalidPreferencesException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public InvalidPreferencesException(List<FieldError> fieldErrors) {
        super("Invalid preferences");
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<FieldError> fieldErrors() { return fieldErrors; }
}
