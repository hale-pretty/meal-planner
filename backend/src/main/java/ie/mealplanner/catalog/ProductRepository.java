package ie.mealplanner.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
            select p from IngredientProductMapping m
            join m.product p
            join fetch p.retailer r
            where m.ingredient.code = :ingredientCode
              and (:retailerCode is null or r.code = :retailerCode)
            order by r.code, p.id
            """)
    List<Product> findMatches(@Param("ingredientCode") String ingredientCode,
                             @Nullable @Param("retailerCode") String retailerCode);
}
