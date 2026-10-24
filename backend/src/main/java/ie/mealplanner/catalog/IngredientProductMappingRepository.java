package ie.mealplanner.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientProductMappingRepository extends JpaRepository<IngredientProductMapping, Long> {
    List<IngredientProductMapping> findAllByIngredientCode(String ingredientCode);
}
