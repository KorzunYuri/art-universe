package yurykorzun.art.universe.music.quiz.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenerationTrackTest {

    @Test
    void builder_shouldCreateGenerationTrackWithAllFields() {
        // when
        GenerationTrack track = GenerationTrack.builder()
            .generationId(1L)
            .trackId(100L)
            .primaryArtistId(50L)
            .trackName("Test Track")
            .artistName("Test Artist")
            .orderIndex(1)
            .build();

        // then
        assertNotNull(track);
        assertEquals(1L, track.getGenerationId());
        assertEquals(100L, track.getTrackId());
        assertEquals(50L, track.getPrimaryArtistId());
        assertEquals("Test Track", track.getTrackName());
        assertEquals("Test Artist", track.getArtistName());
        assertEquals(1, track.getOrderIndex());
        assertNotNull(track.getCreatedAt());
        assertNotNull(track.getUpdatedAt());
    }
}
