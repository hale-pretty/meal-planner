package ie.mealplanner.catalog;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerRepository extends JpaRepository<Retailer, Long> {
    Optional<Retailer> findByCode(String code);
    List<Retailer> findAllByOrderByNameAscCodeAsc();
}
