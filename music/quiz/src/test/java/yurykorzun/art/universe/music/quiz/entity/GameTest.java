package yurykorzun.art.universe.music.quiz.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void builder_shouldCreateGame() {
        // when
        Game game = Game.builder().build();

        // then
        assertNotNull(game);
        assertNotNull(game.getCreatedAt());
        assertNotNull(game.getUpdatedAt());
    }
}
