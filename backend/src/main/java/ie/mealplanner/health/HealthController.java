package ie.mealplanner.health;

import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final JdbcTemplate database;

    public HealthController(DataSource dataSource) {
        database = new JdbcTemplate(dataSource);
        database.setQueryTimeout(1);
    }

    @GetMapping(value = "/api/v1/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthResponse> health() {
        try {
            Integer result = database.queryForObject("SELECT 1", Integer.class);
            if (Integer.valueOf(1).equals(result)) {
                return ResponseEntity.ok(new HealthResponse("UP"));
            }
        } catch (DataAccessException unavailable) {
            // Health responses intentionally contain no database connection details.
        }
        return ResponseEntity.status(503).body(new HealthResponse("DOWN"));
    }
}
