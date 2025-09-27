package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationStepDto;
import yurykorzun.art.universe.music.quiz.entity.Game;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.GameRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import yurykorzun.art.universe.music.quiz.repository.impl.PipelineRepositoryImpl;
import yurykorzun.art.universe.music.quiz.service.impl.GenerationServiceImpl;
import yurykorzun.art.universe.music.quiz.config.StepProcessorConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        GenerationServiceImpl.class,
        PipelineRepositoryImpl.class,
        StepProcessorConfig.class
})
class GenerationServiceIntegrationTest extends JpaOnlyTest {

    @Autowired
    private GenerationService generationService;

    @Autowired
    private GameRepository gameRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    @Commit
    void setUp() {
        // Clean up test data
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.generation_track CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.generation CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.game CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.track CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_view.v_track CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_view.v_artist CASCADE").executeUpdate();

        // Insert test data
        entityManager.createNativeQuery("INSERT INTO mu_view.v_artist (id, name) VALUES (1, 'Test Artist 1'), (2, 'Test Artist 2')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO mu_view.v_track (id, primary_artist_id, name) VALUES (1, 1, 'Track 1'), (2, 1, 'Track 2'), (3, 2, 'Track 3')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO mu_quiz.track (master_id) VALUES (1), (2), (3)").executeUpdate();
    }

    @Test
    void generateTracks_shouldCreateGenerationWithTracks_whenValidData() {
        // given
        Game game = gameRepository.save(Game.builder().build());
        
        // Create final step with targetCount
        GenerationStepDto finalStep = new GenerationStepDto();
        finalStep.setType(GenerationStepType.FINAL_SELECTION);
        finalStep.setParams(Map.of("targetCount", 2));
        List<GenerationStepDto> steps = List.of(finalStep);

        // when
        GenerationDto result = generationService.generateTracks(game.getId(), steps);

        // then
        assertNotNull(result);
        assertEquals(game.getId(), result.getGameId());
        assertEquals(2, result.getTargetCount());
        assertEquals(GenerationStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getResultTableName());

        // Verify tracks were generated
        Long trackCount = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM mu_quiz.generation_track WHERE generation_id = :generationId")
            .setParameter("generationId", result.getId())
            .getSingleResult();
        assertTrue(trackCount > 0);
        assertTrue(trackCount <= 2);
    }

    @Test
    void generateTracks_shouldHandleEmptyData_gracefully() {
        // given
        Game game = gameRepository.save(Game.builder().build());
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.track CASCADE").executeUpdate();

        // Create final step with targetCount
        GenerationStepDto finalStep = new GenerationStepDto();
        finalStep.setType(GenerationStepType.FINAL_SELECTION);
        finalStep.setParams(Map.of("targetCount", 5));
        List<GenerationStepDto> steps = List.of(finalStep);

        // when
        GenerationDto result = generationService.generateTracks(game.getId(), steps);

        // then
        assertEquals(GenerationStatus.COMPLETED, result.getStatus());
        
        Long trackCount = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM mu_quiz.generation_track WHERE generation_id = :generationId")
            .setParameter("generationId", result.getId())
            .getSingleResult();
        assertEquals(0, trackCount);
    }

    @Test
    void generateTracks_shouldHandleError_whenStepProcessingFails() {
        // given
        Game game = gameRepository.save(Game.builder().build());
        
        // Create invalid step configuration that will cause error during step processing
        GenerationStepDto invalidStep = new GenerationStepDto();
        invalidStep.setType(GenerationStepType.FINAL_SELECTION);
        invalidStep.setParams(Map.of("targetCount", -1)); // Invalid target count that will cause DB constraint error
        List<GenerationStepDto> steps = List.of(invalidStep);

        // when & then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> generationService.generateTracks(game.getId(), steps)
        );
        
        assertEquals("Track generation failed", exception.getMessage());
    }
}
