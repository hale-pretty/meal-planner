package ie.mealplanner.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Validate with Jakarta Validator; enclosing recipe DTOs must use @Valid for nested values. */
public record RecipeIngredient(
        @NotBlank(message = "Choose an ingredient code") String ingredientCode,
        @NotNull(message = "Provide a quantity") @Positive(message = "Quantity must be positive") BigDecimal quantity,
        @NotNull(message = "Choose a quantity unit") QuantityUnit unit) { }
