package yurykorzun.art.universe.music.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseMvcTest;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.service.ArtistService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistController.class)
class ArtistControllerTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistService artistService;

    @Test
    void bind_shouldReturnBindingDto_whenSuccessful() throws Exception {
        // given
        Long masterId = 1L;
        BindingDto expectedResult = BindingDto.builder()
            .masterId(masterId)
            .isBound(true)
            .bindingId(100L)
            .build();

        when(artistService.bind(masterId)).thenReturn(expectedResult);

        // when & then
        mockMvc.perform(post("/api/v1/artists/{masterId}/bind", masterId))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        verify(artistService).bind(masterId);
    }

    @Test
    void unbind_shouldReturnBindingDto_whenSuccessful() throws Exception {
        // given
        Long masterId = 1L;
        BindingDto expectedResult = BindingDto.builder()
            .masterId(masterId)
            .isBound(false)
            .bindingId(null)
            .build();

        when(artistService.unbind(masterId)).thenReturn(expectedResult);

        // when & then
        mockMvc.perform(delete("/api/v1/artists/{masterId}/bind", masterId))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        verify(artistService).unbind(masterId);
    }

    @Test
    void getBinding_shouldReturnBindingDto_whenSuccessful() throws Exception {
        // given
        Long masterId = 1L;
        BindingDto expectedResult = BindingDto.builder()
            .masterId(masterId)
            .isBound(true)
            .bindingId(100L)
            .build();

        when(artistService.getBinding(masterId)).thenReturn(expectedResult);

        // when & then
        mockMvc.perform(get("/api/v1/artists/{masterId}/binding", masterId))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        verify(artistService).getBinding(masterId);
    }

    @Test
    void getBindings_shouldReturnListOfBindingDto_whenSuccessful() throws Exception {
        // given
        List<Long> masterIds = List.of(1L, 2L, 3L);
        List<BindingDto> expectedResults = List.of(
            BindingDto.builder().masterId(1L).isBound(true).bindingId(100L).build(),
            BindingDto.builder().masterId(2L).isBound(false).bindingId(null).build(),
            BindingDto.builder().masterId(3L).isBound(true).bindingId(300L).build()
        );

        when(artistService.getBindings(masterIds)).thenReturn(expectedResults);

        // when & then
        mockMvc.perform(post("/api/v1/artists/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(masterIds)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResults)));

        verify(artistService).getBindings(masterIds);
    }

    @Test
    void bind_shouldReturnInternalServerError_whenServiceThrowsRuntimeException() throws Exception {
        // given
        Long masterId = 1L;
        when(artistService.bind(masterId)).thenThrow(new RuntimeException("Test error"));

        // when & then
        mockMvc.perform(post("/api/v1/artists/{masterId}/bind", masterId))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Details can be found in server logs."));

        verify(artistService).bind(masterId);
    }

    @Test
    void unbind_shouldReturnInternalServerError_whenServiceThrowsRuntimeException() throws Exception {
        // given
        Long masterId = 1L;
        when(artistService.unbind(masterId)).thenThrow(new RuntimeException("Test error"));

        // when & then
        mockMvc.perform(delete("/api/v1/artists/{masterId}/bind", masterId))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Details can be found in server logs."));

        verify(artistService).unbind(masterId);
    }

    @Test
    void getBinding_shouldReturnInternalServerError_whenServiceThrowsRuntimeException() throws Exception {
        // given
        Long masterId = 1L;
        when(artistService.getBinding(masterId)).thenThrow(new RuntimeException("Test error"));

        // when & then
        mockMvc.perform(get("/api/v1/artists/{masterId}/binding", masterId))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Details can be found in server logs."));

        verify(artistService).getBinding(masterId);
    }

    @Test
    void getBindings_shouldReturnInternalServerError_whenServiceThrowsRuntimeException() throws Exception {
        // given
        List<Long> masterIds = List.of(1L, 2L);
        when(artistService.getBindings(masterIds)).thenThrow(new RuntimeException("Test error"));

        // when & then
        mockMvc.perform(post("/api/v1/artists/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(masterIds)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Details can be found in server logs."));

        verify(artistService).getBindings(masterIds);
    }

    @Test
    void bind_shouldReturnBadRequest_whenInvalidPathVariable() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/artists/{masterId}/bind", "invalid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Parameter 'masterId' should be of type Long"));
    }

    @Test
    void getBindings_shouldReturnBadRequest_whenMalformedJson() throws Exception {
        // when & then
        String malformedJson = "invalid json";
        mockMvc.perform(post("/api/v1/artists/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Malformed JSON request. Please check your request body."));
    }

    @Test
    void bind_shouldReturnBadRequest_whenIllegalArgumentException() throws Exception {
        // given
        Long masterId = 1L;
        when(artistService.bind(masterId)).thenThrow(new IllegalArgumentException("Invalid master ID"));

        // when & then
        mockMvc.perform(post("/api/v1/artists/{masterId}/bind", masterId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid argument: Invalid master ID"));

        verify(artistService).bind(masterId);
    }
}
