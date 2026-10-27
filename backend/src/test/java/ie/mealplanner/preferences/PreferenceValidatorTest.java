package ie.mealplanner.preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.mealplanner.catalog.*;
import ie.mealplanner.recipe.*;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreferenceValidatorTest {
    private static ValidatorFactory factory;
    private final IngredientRepository ingredients = mock(IngredientRepository.class);
    private final RetailerRepository retailers = mock(RetailerRepository.class);
    private PreferenceValidator validator;

    @BeforeAll static void factory() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }
    @BeforeEach void setup() {
        validator = new PreferenceValidator(factory.getValidator(), ingredients, retailers);
        when(ingredients.findAllByCodeIn(any())).thenReturn(List.of());
    }

    private PreferenceRequest request(Integer servings, Integer time, List<String> exclusions, String retailer) {
        return new PreferenceRequest(Cuisine.ANY, MealType.DINNER, servings, time, Diet.NONE, exclusions, retailer);
    }

    @Test void defaultsAndEmptyExclusionsAreValid() {
        assertThat(validator.validate(PreferenceChoices.defaults())).isEqualTo(PreferenceChoices.defaults());
    }

    @ParameterizedTest @ValueSource(ints = {1, 8})
    void acceptsServingBoundaries(int servings) {
        assertThat(validator.validate(request(servings, 30, List.of(), null)).servings()).isEqualTo(servings);
    }

    @ParameterizedTest @ValueSource(ints = {-1, 0, 9})
    void rejectsServingBounds(int servings) {
        assertThatThrownBy(() -> validator.validate(request(servings, 30, List.of(), null)))
                .isInstanceOf(InvalidPreferencesException.class);
    }

    @Test void allFixedChoicesAndCookingTimesAreAccepted() {
        for (Cuisine cuisine : Cuisine.values()) for (MealType meal : MealType.values())
            for (Diet diet : Diet.values()) for (int time : PreferenceChoices.COOKING_TIMES) {
                validator.validate(new PreferenceRequest(cuisine, meal, 2, time, diet, List.of(), null));
            }
    }

    @Test void missingFieldsProduceFieldErrorsWithoutCatalogAccess() {
        var missing = new PreferenceRequest(null, null, null, null, null, null, null);
        assertThatThrownBy(() -> validator.validate(missing)).isInstanceOfSatisfying(InvalidPreferencesException.class,
                e -> assertThat(e.fieldErrors()).extracting(v -> v.field()).containsExactly(
                        "cuisine", "diet", "excludedIngredientCodes", "maxCookingTimeMinutes", "mealType", "servings"));
        assertThatThrownBy(() -> validator.validate(null)).isInstanceOf(InvalidPreferencesException.class);
        verifyNoInteractions(ingredients, retailers);
    }

    @Test void rejectsUnsupportedTimeUnknownCatalogCodesAndBlankSelections() {
        for (PreferenceRequest invalid : List.of(request(2, 20, List.of(), null),
                request(2, 30, List.of("unknown-secret"), null), request(2, 30, List.of(" "), null),
                request(2, 30, Arrays.asList((String) null), null), request(2, 30, List.of(), ""),
                request(2, 30, List.of(), "unknown-secret"))) {
            assertThatThrownBy(() -> validator.validate(invalid)).isInstanceOfSatisfying(InvalidPreferencesException.class,
                    e -> assertThat(e.fieldErrors().toString()).doesNotContain("unknown-secret"));
        }
    }

    @Test void deduplicatesKnownIngredientsAndAcceptsRetailer() {
        when(ingredients.findAllByCodeIn(List.of("fish-sauce")))
                .thenReturn(List.of(new Ingredient("fish-sauce", "Fish sauce", "liquid")));
        when(retailers.findByCode("tesco")).thenReturn(Optional.of(new Retailer("tesco", "Tesco", "https://example.invalid")));
        var valid = validator.validate(request(2, 30, List.of("fish-sauce", "fish-sauce"), "tesco"));
        assertThat(valid.excludedIngredientCodes()).containsExactly("fish-sauce");
        assertThatThrownBy(() -> valid.excludedIngredientCodes().add("another")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test void enumCodesRejectUnknownBlankAndUppercaseWithoutFallback() {
        var mapper = new ObjectMapper();
        for (Class<?> type : List.of(Cuisine.class, MealType.class, Diet.class)) {
            for (String code : List.of("unknown", "", " ", "ANY")) {
                assertThatThrownBy(() -> mapper.readValue(mapper.writeValueAsString(code), type))
                        .isInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);
            }
        }
        assertThat(Cuisine.ANY.code()).isEqualTo("any"); // Preference sentinel only; T06 must choose concrete recipes.
    }

    record NestedIngredient(@Valid RecipeIngredient ingredient) { }

    @Test void validatesPositiveDecimalQuantitiesAndNestedValuesForEveryUnit() {
        var beanValidator = factory.getValidator();
        for (QuantityUnit unit : QuantityUnit.values()) {
            assertThat(beanValidator.validate(new NestedIngredient(new RecipeIngredient("fish-sauce", new BigDecimal("0.25"), unit)))).isEmpty();
        }
        for (BigDecimal quantity : Arrays.asList(null, BigDecimal.ZERO, new BigDecimal("-0.1"))) {
            assertThat(beanValidator.validate(new NestedIngredient(new RecipeIngredient("fish-sauce", quantity, QuantityUnit.ML)))).hasSize(1);
        }
        assertThat(beanValidator.validate(new NestedIngredient(new RecipeIngredient(" ", BigDecimal.ONE, null)))).hasSize(2);
    }
}
