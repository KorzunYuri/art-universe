package yurykorzun.art.universe.music.quiz.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.entity.Game;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameRepositoryTest extends JpaOnlyTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
    }

    @Test
    void save_shouldPersistGame_whenValidData() {
        // given
        Game game = Game.builder().build();

        // when
        Game savedGame = gameRepository.save(game);
        entityManager.flush();

        // then
        assertNotNull(savedGame.getId());
        assertNotNull(savedGame.getCreatedAt());
    }

    @Test
    void findById_shouldReturnGame_whenExists() {
        // given
        Game game = Game.builder().build();
        Game savedGame = gameRepository.save(game);
        entityManager.flush();

        // when
        Optional<Game> result = gameRepository.findById(savedGame.getId());

        // then
        assertTrue(result.isPresent());
        assertEquals(savedGame.getId(), result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        // when
        Optional<Game> result = gameRepository.findById(999L);

        // then
        assertFalse(result.isPresent());
    }
}
