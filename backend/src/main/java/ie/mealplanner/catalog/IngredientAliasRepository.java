package ie.mealplanner.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientAliasRepository extends JpaRepository<IngredientAlias, Long> {
    List<IngredientAlias> findAllByIngredientCode(String ingredientCode);
}
