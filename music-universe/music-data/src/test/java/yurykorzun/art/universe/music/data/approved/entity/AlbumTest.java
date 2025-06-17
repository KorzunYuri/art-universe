package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidAlbum() {
        String name = "OK Computer";
        Long primaryArtistId = 1L;

        Album album = Album.builder()
            .name(name)
            .primaryArtistId(primaryArtistId)
            .build();

        assertNull(album.getId()); // ID is null before persistence
        assertEquals(name, album.getName());
        assertEquals(primaryArtistId, album.getPrimaryArtistId());
        assertNull(album.getAlbumGroupId());
    }

    @Test
    void whenAllFieldsAreSet_shouldCreateValidAlbum() {
        String name = "OK Computer";
        Long primaryArtistId = 1L;
        Long albumGroupId = 2L;

        Album album = Album.builder()
            .name(name)
            .primaryArtistId(primaryArtistId)
            .albumGroupId(albumGroupId)
            .build();

        assertEquals(name, album.getName());
        assertEquals(primaryArtistId, album.getPrimaryArtistId());
        assertEquals(albumGroupId, album.getAlbumGroupId());
    }

    @Test
    void whenNameNotSet_shouldFailToCreateAlbum() {
        assertThrows(NullPointerException.class, () -> Album.builder().name(null));
    }

    @Test
    void whenPrimaryArtistIdNotSet_shouldFailToCreateAlbum() {
        assertThrows(NullPointerException.class, () -> Album.builder().primaryArtistId(null));
    }
}
