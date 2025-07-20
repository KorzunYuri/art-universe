package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrackTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidTrack() {
        String name = "Paranoid Android";
        Long primaryArtistId = 1L;

        Track track = Track.builder()
            .name(name)
            .primaryArtistId(primaryArtistId)
            .build();

        assertNull(track.getId()); // ID is null before persistence
        assertEquals(name, track.getName());
        assertEquals(primaryArtistId, track.getPrimaryArtistId());
        assertNotNull(track.getCreatedAt());
        assertNotNull(track.getUpdatedAt());
    }

    @Test
    void whenNameNotSet_shouldFailToCreateTrack() {
        assertThrows(NullPointerException.class, () -> Track.builder().name(null));
    }

    @Test
    void whenPrimaryArtistIdNotSet_shouldFailToCreateTrack() {
        assertThrows(NullPointerException.class, () -> Track.builder().primaryArtistId(null));
    }
}
