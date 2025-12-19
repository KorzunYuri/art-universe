package yurykorzun.art.universe.music.quiz.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.Generation;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.entity.GenerationTrack;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.impl.GenerationServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationServiceTest {

    @Mock
    private GenerationRepository generationRepository;

    @Mock
    private GenerationTrackRepository generationTrackRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private PipelineStepRepository pipelineStepRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private PipelineRunRepository pipelineRunRepository;

    @Mock
    private PipelineService pipelineService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private GenerationServiceImpl generationService;

    @Test
    void approveGeneration_shouldReturnApprovedGeneration_whenGenerationExists() {
        // given
        Long generationId = 1L;
        Generation generation = Generation.builder()
            .gameId(1L)
            .pipelineId(1L)
            .pipelineRunId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .build();
        
        Generation approvedGeneration = Generation.builder()
            .gameId(1L)
            .pipelineId(1L)
            .pipelineRunId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(true)
            .build();
        
        when(generationRepository.findById(generationId)).thenReturn(Optional.of(generation));
        when(generationRepository.save(any(Generation.class))).thenReturn(approvedGeneration);

        // when
        GenerationDto result = generationService.approveGeneration(generationId);

        // then
        assertNotNull(result);
        assertTrue(result.getApproved());
        verify(generationRepository).findById(generationId);
        verify(generationRepository).save(any(Generation.class));
    }

    @Test
    void disapproveGeneration_shouldReturnDisapprovedGeneration_whenGenerationExists() {
        // given
        Long generationId = 1L;
        Generation generation = Generation.builder()
            .gameId(1L)
            .pipelineId(1L)
            .pipelineRunId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(true)
            .build();
        
        Generation disapprovedGeneration = Generation.builder()
            .gameId(1L)
            .pipelineId(1L)
            .pipelineRunId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .build();
        
        when(generationRepository.findById(generationId)).thenReturn(Optional.of(generation));
        when(generationRepository.save(any(Generation.class))).thenReturn(disapprovedGeneration);

        // when
        GenerationDto result = generationService.disapproveGeneration(generationId);

        // then
        assertNotNull(result);
        assertFalse(result.getApproved());
        verify(generationRepository).findById(generationId);
        verify(generationRepository).save(any(Generation.class));
    }

    @Test
    void approveGeneration_shouldThrowException_whenGenerationNotFound() {
        // given
        Long generationId = 999L;
        when(generationRepository.findById(generationId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.approveGeneration(generationId)
        );
        
        assertEquals("Generation not found: 999", exception.getMessage());
        verify(generationRepository).findById(generationId);
        verify(generationRepository, never()).save(any());
    }

    @Test
    void removeTrackFromGeneration_shouldRemoveTrack_whenGenerationExists() {
        // given
        Long generationId = 1L;
        Long trackId = 100L;
        
        when(generationRepository.existsById(generationId)).thenReturn(true);

        // when
        generationService.removeTrackFromGeneration(generationId, trackId);

        // then
        verify(generationRepository).existsById(generationId);
        verify(generationTrackRepository).deleteByGenerationIdAndTrackId(generationId, trackId);
    }

    @Test
    void removeTrackFromGeneration_shouldThrowException_whenGenerationNotFound() {
        // given
        Long generationId = 999L;
        Long trackId = 100L;
        
        when(generationRepository.existsById(generationId)).thenReturn(false);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.removeTrackFromGeneration(generationId, trackId)
        );
        
        assertEquals("Generation not found: 999", exception.getMessage());
        verify(generationRepository).existsById(generationId);
        verify(generationTrackRepository, never()).deleteByGenerationIdAndTrackId(any(), any());
    }

    @Test
    void getGenerations_shouldReturnGenerationList_whenGenerationsExist() {
        // given
        Long gameId = 1L;
        List<Generation> generations = List.of(
            Generation.builder()
                .gameId(gameId)
                .pipelineId(1L)
                .pipelineRunId(1L)
                .targetCount(20)
                .status(GenerationStatus.COMPLETED)
                .approved(true)
                .build(),
            Generation.builder()
                .gameId(gameId)
                .pipelineId(2L)
                .pipelineRunId(2L)
                .targetCount(15)
                .status(GenerationStatus.PENDING)
                .approved(false)
                .build()
        );

        when(generationRepository.findByGameIdOrderByCreatedAtDesc(gameId)).thenReturn(generations);

        // when
        List<GenerationDto> result = generationService.getGenerations(gameId);

        // then
        assertEquals(2, result.size());
        verify(generationRepository).findByGameIdOrderByCreatedAtDesc(gameId);
    }

    @Test
    void getGenerationTracks_shouldReturnTrackList_whenTracksExist() {
        // given
        Long generationId = 1L;
        List<GenerationTrack> tracks = List.of(
            GenerationTrack.builder()
                .generationId(generationId)
                .trackId(100L)
                .trackName("Track 1")
                .artistName("Artist 1")
                .orderIndex(1)
                .build(),
            GenerationTrack.builder()
                .generationId(generationId)
                .trackId(200L)
                .trackName("Track 2")
                .artistName("Artist 2")
                .orderIndex(2)
                .build()
        );

        when(generationTrackRepository.findByGenerationIdOrderByOrderIndex(generationId)).thenReturn(tracks);

        // when
        List<GenerationTrackDto> result = generationService.getGenerationTracks(generationId);

        // then
        assertEquals(2, result.size());
        assertEquals("Track 1", result.get(0).getTrackName());
        assertEquals("Track 2", result.get(1).getTrackName());
        verify(generationTrackRepository).findByGenerationIdOrderByOrderIndex(generationId);
    }
}
