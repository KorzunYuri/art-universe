package yurykorzun.art.universe.music.quiz.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.service.GenerationService;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationControllerTest {

    @Mock
    private GenerationService generationService;

    @Mock
    private PipelineService pipelineService;

    @InjectMocks
    private GenerationController generationController;

    @Test
    void generateTracks_shouldDelegateToService() {
        // given
        Long gameId = 1L;
        GenerationDto expectedDto = GenerationDto.builder()
            .id(1L)
            .gameId(gameId)
            .status(GenerationStatus.COMPLETED)
            .build();

        when(generationService.generateTracks(gameId)).thenReturn(expectedDto);

        // when
        GenerationDto result = generationController.generateTracks(gameId);

        // then
        assertEquals(expectedDto, result);
        verify(generationService).generateTracks(gameId);
    }

    @Test
    void getGenerations_shouldDelegateToService() {
        // given
        Long gameId = 1L;
        List<GenerationDto> expectedList = List.of(
            GenerationDto.builder().id(1L).gameId(gameId).status(GenerationStatus.COMPLETED).build()
        );

        when(generationService.getGenerations(gameId)).thenReturn(expectedList);

        // when
        List<GenerationDto> result = generationController.getGenerations(gameId);

        // then
        assertEquals(1, result.size());
        verify(generationService).getGenerations(gameId);
    }

    @Test
    void getGenerationTracks_shouldDelegateToService() {
        // given
        Long generationId = 1L;
        List<GenerationTrackDto> expectedList = List.of(
            GenerationTrackDto.builder().trackId(100L).trackName("Track").artistName("Artist").orderIndex(1).build()
        );

        when(generationService.getGenerationTracks(generationId)).thenReturn(expectedList);

        // when
        List<GenerationTrackDto> result = generationController.getGenerationTracks(generationId);

        // then
        assertEquals(1, result.size());
        verify(generationService).getGenerationTracks(generationId);
    }

    @Test
    void getGenerationPipeline_shouldReturnPipeline_whenGenerationHasPipeline() {
        // given
        Long generationId = 1L;
        Long pipelineId = 10L;
        GenerationDto generationDto = GenerationDto.builder()
            .id(generationId)
            .gameId(1L)
            .pipelineId(pipelineId)
            .status(GenerationStatus.COMPLETED)
            .build();

        PipelineDto expectedPipeline = PipelineDto.builder()
            .id(pipelineId)
            .immutable(true)
            .steps(List.of())
            .build();

        when(generationService.getGenerations(null)).thenReturn(List.of(generationDto));
        when(pipelineService.getPipeline(pipelineId)).thenReturn(expectedPipeline);

        // when
        PipelineDto result = generationController.getGenerationPipeline(generationId);

        // then
        assertEquals(expectedPipeline, result);
        verify(pipelineService).getPipeline(pipelineId);
    }

    @Test
    void getGenerationPipeline_shouldThrow_whenGenerationNotFound() {
        // given
        when(generationService.getGenerations(null)).thenReturn(List.of());

        // when & then
        assertThrows(IllegalArgumentException.class,
            () -> generationController.getGenerationPipeline(999L));
    }

    @Test
    void getGenerationPipeline_shouldThrow_whenNoPipeline() {
        // given
        Long generationId = 1L;
        GenerationDto generationDto = GenerationDto.builder()
            .id(generationId)
            .gameId(1L)
            .pipelineId(null)
            .status(GenerationStatus.COMPLETED)
            .build();

        when(generationService.getGenerations(null)).thenReturn(List.of(generationDto));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> generationController.getGenerationPipeline(generationId));

        assertEquals("Generation has no associated pipeline", exception.getMessage());
    }

    @Test
    void approveGeneration_shouldReturnApprovedGeneration_whenSuccessful() {
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

        // when
        GenerationDto result = generationController.approveGeneration(generationId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertTrue(result.getApproved());
        verify(generationService).approveGeneration(generationId);
    }

    @Test
    void disapproveGeneration_shouldReturnDisapprovedGeneration_whenSuccessful() {
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

        // when
        GenerationDto result = generationController.disapproveGeneration(generationId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertFalse(result.getApproved());
        verify(generationService).disapproveGeneration(generationId);
    }

    @Test
    void removeTrackFromGeneration_shouldCallService_whenSuccessful() {
        // given
        Long generationId = 1L;
        Long trackId = 100L;

        // when
        generationController.removeTrackFromGeneration(generationId, trackId);

        // then
        verify(generationService).removeTrackFromGeneration(generationId, trackId);
    }
}
