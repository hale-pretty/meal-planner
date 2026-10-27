package ie.mealplanner.api;

import java.util.List;
import java.util.UUID;

public record ErrorResponse(String code, String message, UUID requestId, List<FieldError> fieldErrors) { }
