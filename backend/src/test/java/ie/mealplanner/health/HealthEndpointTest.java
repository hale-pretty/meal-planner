package ie.mealplanner.health;

import ie.mealplanner.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointTest extends PostgresIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void applicationStartsAndReturnsHealthyStatusOverHttp() {
        ResponseEntity<HealthResponse> response = restTemplate.getForEntity(
                "/api/v1/health", HealthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType())
                .isNotNull()
                .satisfies(contentType -> assertThat(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue());
        assertThat(response.getBody()).isEqualTo(new HealthResponse("UP"));
    }
}

