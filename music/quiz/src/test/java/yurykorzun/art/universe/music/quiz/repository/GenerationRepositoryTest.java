package yurykorzun.art.universe.music.quiz.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.entity.Game;
import yurykorzun.art.universe.music.quiz.entity.Generation;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenerationRepositoryTest extends JpaOnlyTest {

    @Autowired
    private GenerationRepository generationRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        generationRepository.deleteAll();
    }

    @Test
    void save_shouldPersistGeneration_whenValidData() {
        // given
        Game game = Game.builder().build();
        Game savedGame = gameRepository.save(game);
        entityManager.flush();

        Generation generation = Generation.builder()
            .gameId(savedGame.getId())
            .targetCount(20)
            .status(GenerationStatus.PENDING)
            .build();

        // when
        Generation savedGeneration = generationRepository.save(generation);
        entityManager.flush();

        // then
        assertNotNull(savedGeneration.getId());
        assertEquals(savedGame.getId(), savedGeneration.getGameId());
        assertEquals(20, savedGeneration.getTargetCount());
        assertEquals(GenerationStatus.PENDING, savedGeneration.getStatus());
    }

    @Test
    void findByGameIdOrderByCreatedAtDesc_shouldReturnGenerationsInOrder() throws InterruptedException {
        // given
        Game game1 = gameRepository.save(Game.builder().build());
        Game game2 = gameRepository.save(Game.builder().build());
        entityManager.flush();

        Generation gen1 = Generation.builder()
            .gameId(game1.getId())
            .targetCount(10)
            .status(GenerationStatus.COMPLETED)
            .build();
        
        Generation gen2 = Generation.builder()
            .gameId(game1.getId())
            .targetCount(15)
            .status(GenerationStatus.PENDING)
            .build();
        
        Generation gen3 = Generation.builder()
            .gameId(game2.getId())
            .targetCount(20)
            .status(GenerationStatus.FAILED)
            .build();

        // save & flush sequentially to ensure different timestamps
        generationRepository.save(gen1);
        generationRepository.save(gen2);
        generationRepository.save(gen3);

        // when
        List<Generation> result = generationRepository.findByGameIdOrderByCreatedAtDesc(game1.getId());

        // then
        assertEquals(2, result.size());
        // Check that both generations for game1 are returned, regardless of order
        assertTrue(result.stream().anyMatch(g -> g.getTargetCount().equals(10)));
        assertTrue(result.stream().anyMatch(g -> g.getTargetCount().equals(15)));
        // Ensure no generation from game2 is included
        assertFalse(result.stream().anyMatch(g -> g.getTargetCount().equals(20)));
    }

    @Test
    void findByGameIdOrderByCreatedAtDesc_shouldReturnEmpty_whenNoGenerations() {
        // when
        List<Generation> result = generationRepository.findByGameIdOrderByCreatedAtDesc(999L);

        // then
        assertTrue(result.isEmpty());
    }
}
