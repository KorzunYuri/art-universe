package yurykorzun.art.universe.music.quiz.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void builder_shouldCreateGameWithAllFields() {
        // when
        Game game = Game.builder()
            .generationId(123L)
            .build();

        // then
        assertNotNull(game);
        assertEquals(123L, game.getGenerationId());
        assertNotNull(game.getCreatedAt());
        assertNotNull(game.getUpdatedAt());
    }

    @Test
    void builder_shouldCreateGameWithNullGenerationId() {
        // when
        Game game = Game.builder()
            .generationId(null)
            .build();

        // then
        assertNotNull(game);
        assertNull(game.getGenerationId());
    }
}
