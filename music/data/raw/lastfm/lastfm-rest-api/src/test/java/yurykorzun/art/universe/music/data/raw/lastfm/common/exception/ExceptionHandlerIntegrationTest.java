package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.exception.ErrorResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestExceptionController.class)
class ExceptionHandlerIntegrationTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        // Given
        String errorMessage = "Test entity not found with id: 999";
        String expectedJson = objectMapper.writeValueAsString(new ErrorResponse(errorMessage));

        // When & Then
        mockMvc.perform(get("/test/entity-not-found/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        // Given
        String errorMessage = "An unexpected error occurred. Details can be found in server logs.";
        String expectedJson = objectMapper.writeValueAsString(new ErrorResponse(errorMessage));

        // When & Then
        mockMvc.perform(get("/test/generic-error")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }
}
