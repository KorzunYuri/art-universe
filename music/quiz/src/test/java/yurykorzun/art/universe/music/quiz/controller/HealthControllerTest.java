package yurykorzun.art.universe.music.quiz.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the HealthController
 */
@WebMvcTest(HealthController.class)
class HealthControllerTest extends BaseMvcTest {

    @Test
    void shouldReturnUpStatus() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
