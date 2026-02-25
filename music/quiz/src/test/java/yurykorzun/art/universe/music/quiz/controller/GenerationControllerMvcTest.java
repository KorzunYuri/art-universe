package yurykorzun.art.universe.music.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.service.GenerationService;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenerationController.class)
class GenerationControllerMvcTest extends BaseMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenerationService generationService;

    @MockitoBean
    private PipelineService pipelineService;

    @Test
    void POST_gameGenerations_shouldReturnGenerationDto_whenSuccessful() throws Exception {
        // given
        Long gameId = 1L;
        
        GenerationDto expectedDto = GenerationDto.builder()
            .id(1L)
            .gameId(gameId)
            .status(GenerationStatus.COMPLETED)
            .createdAt(Instant.now())
            .build();

        when(generationService.generateTracks(gameId)).thenReturn(expectedDto);

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(post("/api/v1/games/{gameId}/generations", gameId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(generationService).generateTracks(gameId);
    }

    @Test
    void GET_gameGenerations_shouldReturnGenerationList_whenSuccessful() throws Exception {
        // given
        Long gameId = 1L;
        List<GenerationDto> expectedList = List.of(
            GenerationDto.builder()
                .id(1L)
                .gameId(gameId)
                .status(GenerationStatus.COMPLETED)
                .createdAt(Instant.now())
                .build()
        );

        when(generationService.getGenerations(gameId)).thenReturn(expectedList);

        String expectedJson = objectMapper.writeValueAsString(expectedList);

        // when & then
        mockMvc.perform(get("/api/v1/games/{gameId}/generations", gameId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(generationService).getGenerations(gameId);
    }

    @Test
    void GET_generationTracks_shouldReturnTrackList_whenSuccessful() throws Exception {
        // given
        Long generationId = 1L;
        List<GenerationTrackDto> expectedList = List.of(
            GenerationTrackDto.builder()
                .trackId(100L)
                .trackName("Test Track")
                .artistName("Test Artist")
                .orderIndex(1)
                .build()
        );

        when(generationService.getGenerationTracks(generationId)).thenReturn(expectedList);

        String expectedJson = objectMapper.writeValueAsString(expectedList);

        // when & then
        mockMvc.perform(get("/api/v1/generations/{generationId}/tracks", generationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(generationService).getGenerationTracks(generationId);
    }

    @Test
    void PATCH_approveGeneration_shouldReturnApprovedDto() throws Exception {
        // given
        Long generationId = 1L;
        GenerationDto expectedDto = GenerationDto.builder()
            .id(generationId)
            .gameId(1L)
            .status(GenerationStatus.COMPLETED)
            .approved(true)
            .createdAt(Instant.now())
            .build();

        when(generationService.approveGeneration(generationId)).thenReturn(expectedDto);

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(patch("/api/v1/generations/{generationId}/approve", generationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(generationService).approveGeneration(generationId);
    }

    @Test
    void PATCH_disapproveGeneration_shouldReturnDisapprovedDto() throws Exception {
        // given
        Long generationId = 1L;
        GenerationDto expectedDto = GenerationDto.builder()
            .id(generationId)
            .gameId(1L)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .createdAt(Instant.now())
            .build();

        when(generationService.disapproveGeneration(generationId)).thenReturn(expectedDto);

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(patch("/api/v1/generations/{generationId}/disapprove", generationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(generationService).disapproveGeneration(generationId);
    }

    @Test
    void DELETE_generationTracks_shouldRemoveTrack_whenSuccessful() throws Exception {
        // given
        Long generationId = 1L;
        Long trackId = 100L;

        // when & then
        mockMvc.perform(delete("/api/v1/generations/{generationId}/tracks/{trackId}", generationId, trackId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(generationService).removeTrackFromGeneration(generationId, trackId);
    }
}
