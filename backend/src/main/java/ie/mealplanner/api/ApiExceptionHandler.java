package ie.mealplanner.api;

import ie.mealplanner.preferences.InvalidPreferencesException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(InvalidPreferencesException.class)
    ResponseEntity<Object> invalidPreferences(InvalidPreferencesException exception) {
        return response(400, "INVALID_PREFERENCES", "Check your preferences", exception.fieldErrors());
    }

    @ExceptionHandler({DataAccessException.class, org.springframework.transaction.CannotCreateTransactionException.class})
    ResponseEntity<Object> databaseUnavailable(Exception exception) {
        return response(503, "DATABASE_UNAVAILABLE", "The catalog is temporarily unavailable", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> unexpected(Exception exception) {
        return response(500, "INTERNAL_ERROR", "The request could not be completed", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            org.springframework.web.bind.MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), "Check this field"))
                .distinct().sorted(java.util.Comparator.comparing(FieldError::field)).toList();
        return response(400, "INVALID_PREFERENCES", "Check your preferences", fields);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String code = status.value() == 400 ? "INVALID_PREFERENCES" : "REQUEST_ERROR";
        // Never serialize framework ProblemDetails, binding values, or exception messages.
        return new ResponseEntity<>(new ErrorResponse(code, "Check the request", UUID.randomUUID(), List.of()), headers, status);
    }

    private ResponseEntity<Object> response(int status, String code, String message, List<FieldError> fields) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, UUID.randomUUID(), fields));
    }
}
