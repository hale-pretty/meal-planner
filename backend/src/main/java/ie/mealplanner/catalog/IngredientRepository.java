package ie.mealplanner.catalog;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByCode(String code);
    List<Ingredient> findAllByOrderByNameAscCodeAsc();
    List<Ingredient> findAllByCodeIn(java.util.Collection<String> codes);
}
