package yurykorzun.art.universe.music.quiz.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenerationTest {

    @Test
    void builder_shouldCreateGenerationWithAllFields() {
        // when
        Generation generation = Generation.builder()
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.PENDING)
            .approved(true)
            .resultTableName("gen_tracks_0001_final")
            .build();

        // then
        assertNotNull(generation);
        assertEquals(1L, generation.getGameId());
        assertEquals(20, generation.getTargetCount());
        assertEquals(GenerationStatus.PENDING, generation.getStatus());
        assertTrue(generation.getApproved());
        assertEquals("gen_tracks_0001_final", generation.getResultTableName());
        assertNotNull(generation.getCreatedAt());
        assertNotNull(generation.getUpdatedAt());
    }

    @Test
    void builder_shouldCreateGenerationWithDefaultApproved() {
        // when
        Generation generation = Generation.builder()
            .gameId(1L)
            .targetCount(15)
            .status(GenerationStatus.FAILED)
            .build();

        // then
        assertNotNull(generation);
        assertEquals(1L, generation.getGameId());
        assertEquals(15, generation.getTargetCount());
        assertEquals(GenerationStatus.FAILED, generation.getStatus());
        assertFalse(generation.getApproved());
        assertNull(generation.getResultTableName());
    }
}
