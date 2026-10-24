package ie.mealplanner.health;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthUnavailableTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private DataSource dataSource;

    @Test
    void databaseFailureReturns503WithoutInternalDetails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("private database connection details"));
        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("{\"status\":\"DOWN\"}"));
    }
}
