package ie.mealplanner.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerRepository extends JpaRepository<Retailer, Long> {
    Optional<Retailer> findByCode(String code);
}
