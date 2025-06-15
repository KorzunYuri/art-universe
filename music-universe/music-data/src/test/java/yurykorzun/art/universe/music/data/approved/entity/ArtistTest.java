package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidArtist() {
        String name = "Radiohead";

        Artist artist = Artist.builder()
            .name(name)
            .build();

        assertNull(artist.getId()); // ID is null before persistence
        assertEquals(name, artist.getName());
    }

    @Test
    void whenNameNotSet_shouldFailToCreateArtist() {
        assertThrows(NullPointerException.class, () -> Artist.builder().name(null).build());
    }
}
