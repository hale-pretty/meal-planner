package ie.mealplanner.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.mealplanner.api.FieldError;
import ie.mealplanner.preferences.InvalidPreferencesException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OptionsController.class)
class OptionsFailureTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockitoBean private OptionsService service;

    @Test void sanitizesDatabaseAndUnexpectedFailuresAndGeneratesRequestIds() throws Exception {
        String secret = "SELECT password FROM private jdbc:postgresql://credential";
        doThrow(new DataAccessResourceFailureException(secret)).when(service).options();
        String database = mvc.perform(get("/api/v1/options")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE")).andReturn().getResponse().getContentAsString();
        doThrow(new IllegalStateException(secret)).when(service).options();
        String unexpected = mvc.perform(get("/api/v1/options")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR")).andReturn().getResponse().getContentAsString();
        for (String body : List.of(database, unexpected)) {
            assertThat(body).doesNotContain(secret, "SELECT", "password", "jdbc:", "Exception", "stackTrace");
            assertThat(UUID.fromString(mapper.readTree(body).get("requestId").asText())).isNotNull();
            assertThat(mapper.readTree(body).size()).isEqualTo(4);
            assertThat(mapper.readTree(body).get("fieldErrors").size()).isZero();
        }
        assertThat(mapper.readTree(database).get("requestId")).isNotEqualTo(mapper.readTree(unexpected).get("requestId"));
    }

    @Test void transactionAcquisitionFailuresAreDatabaseFailures() throws Exception {
        doThrow(new org.springframework.transaction.CannotCreateTransactionException("private connection details"))
                .when(service).options();
        mvc.perform(get("/api/v1/options")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"));
    }

    @Test void invalidPreferencesHaveSafeFieldErrors() throws Exception {
        doThrow(new InvalidPreferencesException(List.of(new FieldError("servings", "Servings must be between 1 and 8")))).when(service).options();
        mvc.perform(get("/api/v1/options")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PREFERENCES"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("servings"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Servings must be between 1 and 8"));
    }

    @Test void preservesMvcStatusesAndHeadersWithoutInternalDetails() throws Exception {
        mvc.perform(post("/api/v1/options")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow")).andExpect(jsonPath("$.code").value("REQUEST_ERROR"));
        mvc.perform(get("/missing-private-path")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Check the request"));
        mvc.perform(get("/api/v1/options").accept("application/xml"))
                .andExpect(status().isNotAcceptable());
    }
}
