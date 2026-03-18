package yurykorzun.art.universe.common.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.config.CommonConfig;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Serialization contract tests for modules that import {@code common:commons-web}. Verifies the project-wide serialization rules.
 * <p>
 * This class lives in the {@code testFixtures} source set of {@code commons-web} so it is
 * <em>not</em> executed during {@code commons-web}'s own test task, but is automatically
 * discovered and run by every Spring Boot web-app module via the
 * {@code art-universe.spring-boot-web-app} convention plugin.
 * <p>
 * Security filters are disabled ({@code addFilters = false}) because this test only validates
 * JSON serialization, not authentication. Without this, modules that depend on
 * {@code commons-security} would get Spring Security's default Basic auth applied.
 */
@WebMvcTest(controllers = WebSerializationTestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CommonConfig.class, CommonWebConfig.class})
public class WebSerializationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void restEndpoint_booleanGetterWithIsPrefix_isNotRenamed() throws Exception {
        mockMvc.perform(get("/commons/test-serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSomething").exists())
                .andExpect(jsonPath("$.something").doesNotExist());
    }

    @Test
    void restEndpoint_instantField_serializedAsISO8601String() throws Exception {
        mockMvc.perform(get("/commons/test-serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.createdAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")));
    }

    @Test
    void restEndpoint_localDateTimeField_serializedAsISO8601String() throws Exception {
        mockMvc.perform(get("/commons/test-serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").isString())
                .andExpect(jsonPath("$.updatedAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")));
    }
}
