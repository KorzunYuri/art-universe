package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.quiz.common.archetypes.BaseQuizJpaTest;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.impl.GenerationServiceImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({GenerationServiceImpl.class})
class GenerationServiceIntegrationTest extends BaseQuizJpaTest {

    @Autowired
    private GenerationService generationService;

    @Autowired
    private GenerationRepository generationRepository;

    @Autowired
    private GenerationTrackRepository generationTrackRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineRunRepository pipelineRunRepository;

    @MockitoBean
    private PipelineService pipelineService;

    @MockitoBean
    private PipelineStepRepository pipelineStepRepository;

    @MockitoBean
    private StepRepository stepRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Game testGame;
    private Pipeline testPipeline;
    private PipelineRun testPipelineRun;
    private Generation testGeneration;

    @BeforeEach
    void setUp() {
        // Create pipeline first
        testPipeline = pipelineRepository.save(Pipeline.builder()
            .immutable(false)
            .build());
        entityManager.flush();
        
        // Create game with pipeline reference
        testGame = gameRepository.save(Game.builder()
            .pipelineId(testPipeline.getId())
            .build());
        entityManager.flush();
        
        // Create pipeline run
        testPipelineRun = pipelineRunRepository.save(PipelineRun.builder()
            .pipelineId(testPipeline.getId())
            .status(ExecutionStatus.COMPLETED)
            .build());
        entityManager.flush();
        
        // Create generation with pipeline run reference
        testGeneration = generationRepository.save(Generation.builder()
            .gameId(testGame.getId())
            .pipelineRunId(testPipelineRun.getId())
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .build());
        entityManager.flush();
    }

    @Test
    void getGenerations_shouldReturnGenerationsForGame() {
        // given
        PipelineRun pipelineRun2 = pipelineRunRepository.save(PipelineRun.builder()
            .pipelineId(testPipeline.getId())
            .status(ExecutionStatus.PENDING)
            .build());
        
        Generation generation2 = generationRepository.save(Generation.builder()
            .gameId(testGame.getId())
            .pipelineRunId(pipelineRun2.getId())
            .targetCount(15)
            .status(GenerationStatus.PENDING)
            .approved(true)
            .build());

        // when
        List<GenerationDto> result = generationService.getGenerations(testGame.getId());

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.getId().equals(testGeneration.getId())));
        assertTrue(result.stream().anyMatch(g -> g.getId().equals(generation2.getId())));
    }

    @Test
    void getGenerations_shouldReturnEmptyList_whenNoGenerations() {
        // given
        Pipeline emptyPipeline = pipelineRepository.save(Pipeline.builder().immutable(false).build());
        Game emptyGame = gameRepository.save(Game.builder().pipelineId(emptyPipeline.getId()).build());

        // when
        List<GenerationDto> result = generationService.getGenerations(emptyGame.getId());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getGenerationTracks_shouldReturnTracksInOrder() {
        // given
        GenerationTrack track1 = generationTrackRepository.save(GenerationTrack.builder()
            .generationId(testGeneration.getId())
            .trackId(100L)
            .primaryArtistId(1L)
            .trackName("Track 1")
            .artistName("Artist 1")
            .orderIndex(2)
            .build());

        GenerationTrack track2 = generationTrackRepository.save(GenerationTrack.builder()
            .generationId(testGeneration.getId())
            .trackId(200L)
            .primaryArtistId(2L)
            .trackName("Track 2")
            .artistName("Artist 2")
            .orderIndex(1)
            .build());

        // when
        List<GenerationTrackDto> result = generationService.getGenerationTracks(testGeneration.getId());

        // then
        assertEquals(2, result.size());
        assertEquals(200L, result.get(0).getTrackId()); // orderIndex 1
        assertEquals(100L, result.get(1).getTrackId()); // orderIndex 2
    }

    @Test
    void getGenerationTracks_shouldReturnEmptyList_whenNoTracks() {
        // when
        List<GenerationTrackDto> result = generationService.getGenerationTracks(testGeneration.getId());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void approveGeneration_shouldSetApprovedToTrue() {
        // when
        GenerationDto result = generationService.approveGeneration(testGeneration.getId());

        // then
        assertTrue(result.getApproved());
        
        Generation updated = generationRepository.findById(testGeneration.getId()).orElseThrow();
        assertTrue(updated.getApproved());
    }

    @Test
    void disapproveGeneration_shouldSetApprovedToFalse() {
        // given
        testGeneration.setApproved(true);
        generationRepository.save(testGeneration);

        // when
        GenerationDto result = generationService.disapproveGeneration(testGeneration.getId());

        // then
        assertFalse(result.getApproved());
        
        Generation updated = generationRepository.findById(testGeneration.getId()).orElseThrow();
        assertFalse(updated.getApproved());
    }

    @Test
    void approveGeneration_shouldThrowException_whenGenerationNotFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.approveGeneration(999L)
        );
        
        assertEquals("Generation not found: 999", exception.getMessage());
    }

    @Test
    void removeTrackFromGeneration_shouldRemoveTrack() {
        // given
        GenerationTrack track = generationTrackRepository.save(GenerationTrack.builder()
            .generationId(testGeneration.getId())
            .trackId(100L)
            .primaryArtistId(1L)
            .trackName("Track 1")
            .artistName("Artist 1")
            .orderIndex(1)
            .build());

        // when
        generationService.removeTrackFromGeneration(testGeneration.getId(), 100L);

        // then
        List<GenerationTrack> remaining = generationTrackRepository.findByGenerationIdOrderByOrderIndex(testGeneration.getId());
        assertTrue(remaining.isEmpty());
    }

    @Test
    void removeTrackFromGeneration_shouldThrowException_whenGenerationNotFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.removeTrackFromGeneration(999L, 100L)
        );
        
        assertEquals("Generation not found: 999", exception.getMessage());
    }
}
