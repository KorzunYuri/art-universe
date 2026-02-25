package yurykorzun.art.universe.music.quiz.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.impl.GenerationServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(generationService, "entityManager", entityManager);
    }

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
    void generateTracks_shouldCreateGenerationAndExecutePipeline_whenGameExists() {
        // given
        Long gameId = 1L;
        Long pipelineId = 10L;
        Long immutablePipelineId = 20L;
        Long pipelineRunId = 30L;

        Game game = Game.builder().pipelineId(pipelineId).build();
        Pipeline immutablePipeline = Pipeline.builder().immutable(true).build();
        // Use reflection to set id since setter is private
        try {
            var idField = Pipeline.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(immutablePipeline, immutablePipelineId);
        } catch (Exception e) {
            fail("Could not set pipeline id");
        }

        PipelineRun completedRun = PipelineRun.builder()
            .pipelineId(immutablePipelineId)
            .resultTableName("mu_quiz.result_table")
            .build();
        try {
            var idField = PipelineRun.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(completedRun, pipelineRunId);
        } catch (Exception e) {
            fail("Could not set pipeline run id");
        }

        Generation savedGeneration = Generation.builder()
            .gameId(gameId)
            .pipelineId(immutablePipelineId)
            .status(GenerationStatus.PENDING)
            .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(pipelineService.createImmutableCopy(pipelineId)).thenReturn(immutablePipeline);
        when(generationRepository.save(any(Generation.class))).thenReturn(savedGeneration);
        when(pipelineService.executePipeline(immutablePipelineId)).thenReturn(completedRun);

        jakarta.persistence.Query nativeQuery = mock(jakarta.persistence.Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of(
            new Object[]{1L, 100L, "Track 1", "Artist 1"},
            new Object[]{2L, 200L, "Track 2", "Artist 2"}
        ));

        // when
        GenerationDto result = generationService.generateTracks(gameId);

        // then
        assertNotNull(result);
        verify(pipelineService).validatePipelineForGeneration(pipelineId);
        verify(pipelineService).createImmutableCopy(pipelineId);
        verify(pipelineService).executePipeline(immutablePipelineId);
        verify(generationTrackRepository).saveAll(anyList());
        verify(generationRepository, times(3)).save(any(Generation.class));
    }

    @Test
    void generateTracks_shouldSetStatusFailed_whenPipelineExecutionFails() {
        // given
        Long gameId = 1L;
        Long pipelineId = 10L;
        Long immutablePipelineId = 20L;

        Game game = Game.builder().pipelineId(pipelineId).build();
        Pipeline immutablePipeline = Pipeline.builder().immutable(true).build();
        try {
            var idField = Pipeline.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(immutablePipeline, immutablePipelineId);
        } catch (Exception e) {
            fail("Could not set pipeline id");
        }

        Generation savedGeneration = Generation.builder()
            .gameId(gameId)
            .pipelineId(immutablePipelineId)
            .status(GenerationStatus.PENDING)
            .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(pipelineService.createImmutableCopy(pipelineId)).thenReturn(immutablePipeline);
        when(generationRepository.save(any(Generation.class))).thenReturn(savedGeneration);
        when(pipelineService.executePipeline(immutablePipelineId)).thenThrow(new RuntimeException("Pipeline failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> generationService.generateTracks(gameId));

        assertEquals("Track generation failed", exception.getMessage());
        verify(generationRepository, atLeast(2)).save(any(Generation.class));
    }

    @Test
    void generateTracks_shouldThrowException_whenGameNotFound() {
        // given
        Long gameId = 999L;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.generateTracks(gameId)
        );

        assertEquals("Game not found: 999", exception.getMessage());
    }

    @Test
    void disapproveGeneration_shouldThrowException_whenGenerationNotFound() {
        // given
        Long generationId = 999L;
        when(generationRepository.findById(generationId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.disapproveGeneration(generationId)
        );

        assertEquals("Generation not found: 999", exception.getMessage());
        verify(generationRepository, never()).save(any());
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
