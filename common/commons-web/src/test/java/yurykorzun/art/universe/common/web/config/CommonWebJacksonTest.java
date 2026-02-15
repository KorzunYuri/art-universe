package yurykorzun.art.universe.common.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.common.config.CommonConfig;
import yurykorzun.art.universe.common.testing.SerializationTestDto;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that REST endpoints in modules importing {@code common:commons-web} serialize
 * responses according to the project rules:
 * <ul>
 *   <li>Boolean getters with {@code is} prefix keep their name (isSomething → "isSomething").</li>
 *   <li>Date/time types are serialized as ISO-8601 strings, not timestamps.</li>
 * </ul>
 * Uses an inline {@link TestController} so this test has no external dependencies.
 */
@WebMvcTest
@Import({CommonConfig.class, CommonWebConfig.class, CommonWebJacksonTest.TestController.class})
class CommonWebJacksonTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void restEndpoint_booleanGetterWithIsPrefix_isNotRenamed() throws Exception {
        mockMvc.perform(get("/test-jackson-serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSomething").exists())
                .andExpect(jsonPath("$.something").doesNotExist());
    }

    @Test
    void restEndpoint_instantField_serializedAsISO8601String() throws Exception {
        mockMvc.perform(get("/test-jackson-serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.createdAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")));
    }

    @Test
    void restEndpoint_localDateTimeField_serializedAsISO8601String() throws Exception {
        mockMvc.perform(get("/test-jackson-serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").isString())
                .andExpect(jsonPath("$.updatedAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")));
    }

    @RestController
    @RequestMapping("/test-jackson-serialization")
    static class TestController {
        @GetMapping
        public SerializationTestDto get() {
            return new SerializationTestDto(
                    true,
                    Instant.parse("2024-01-15T10:30:00Z"),
                    LocalDateTime.of(2024, 1, 15, 10, 30, 0)
            );
        }
    }
}
