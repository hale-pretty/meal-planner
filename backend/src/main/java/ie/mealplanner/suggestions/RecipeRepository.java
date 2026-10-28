package ie.mealplanner.suggestions;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Native read queries keep filtering, ordering, and limits in PostgreSQL without mapping child tables as entities. */
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {
    @Query(value = """
            SELECT r.id AS id, r.code AS code, r.name AS name, r.description AS description,
                   r.cuisine AS cuisine, r.meal_type AS "mealType", r.servings AS servings,
                   r.cooking_time_minutes AS "cookingTimeMinutes", r.vegetarian AS vegetarian, r.vegan AS vegan
            FROM recipe r
            WHERE r.meal_type = :mealType
              AND (:cuisine = 'any' OR r.cuisine = :cuisine)
              AND r.cooking_time_minutes <= :maxCookingTime
              AND (:diet = 'none' OR (:diet = 'vegetarian' AND r.vegetarian)
                   OR (:diet = 'vegan' AND r.vegan))
            ORDER BY r.code
            LIMIT 3
            """, nativeQuery = true)
    List<RecipeSummary> findCandidates(@Param("mealType") String mealType, @Param("cuisine") String cuisine,
            @Param("maxCookingTime") int maxCookingTime, @Param("diet") String diet);

    @Query(value = """
            SELECT r.id AS id, r.code AS code, r.name AS name, r.description AS description,
                   r.cuisine AS cuisine, r.meal_type AS "mealType", r.servings AS servings,
                   r.cooking_time_minutes AS "cookingTimeMinutes", r.vegetarian AS vegetarian, r.vegan AS vegan
            FROM recipe r
            WHERE r.meal_type = :mealType
              AND (:cuisine = 'any' OR r.cuisine = :cuisine)
              AND r.cooking_time_minutes <= :maxCookingTime
              AND (:diet = 'none' OR (:diet = 'vegetarian' AND r.vegetarian)
                   OR (:diet = 'vegan' AND r.vegan))
              AND NOT EXISTS (
                  SELECT 1 FROM recipe_ingredient ri
                  JOIN ingredient i ON i.id = ri.ingredient_id
                  WHERE ri.recipe_id = r.id AND i.code IN (:excludedCodes)
              )
            ORDER BY r.code
            LIMIT 3
            """, nativeQuery = true)
    List<RecipeSummary> findCandidatesExcluding(@Param("mealType") String mealType, @Param("cuisine") String cuisine,
            @Param("maxCookingTime") int maxCookingTime, @Param("diet") String diet,
            @Param("excludedCodes") List<String> excludedCodes);

    @Query(value = """
            SELECT i.code AS code, i.name AS name, i.form AS form, ri.quantity AS quantity, ri.unit AS unit
            FROM recipe_ingredient ri JOIN ingredient i ON i.id = ri.ingredient_id
            WHERE ri.recipe_id = :recipeId ORDER BY ri.position
            """, nativeQuery = true)
    List<IngredientRow> findIngredients(@Param("recipeId") long recipeId);

    @Query(value = """
            SELECT step_number AS number, instruction AS instruction
            FROM recipe_step WHERE recipe_id = :recipeId ORDER BY step_number
            """, nativeQuery = true)
    List<StepRow> findSteps(@Param("recipeId") long recipeId);

    interface RecipeSummary {
        Long getId();
        String getCode();
        String getName();
        String getDescription();
        String getCuisine();
        String getMealType();
        int getServings();
        int getCookingTimeMinutes();
        boolean getVegetarian();
        boolean getVegan();
    }

    interface IngredientRow {
        String getCode();
        String getName();
        String getForm();
        BigDecimal getQuantity();
        String getUnit();
    }

    interface StepRow {
        int getNumber();
        String getInstruction();
    }
}
