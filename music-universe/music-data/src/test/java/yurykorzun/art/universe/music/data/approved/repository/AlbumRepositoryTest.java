package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.entity.Album;
import yurykorzun.art.universe.music.data.approved.entity.Artist;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumRepositoryTest extends JpaOnlyTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void whenSavingAlbum_shouldPersistCorrectly() {
        // Given
        Artist artist = Artist.builder().name("Radiohead").build();
        artistRepository.save(artist);

        Album album = Album.builder()
            .name("OK Computer")
            .primaryArtistId(artist.getId())
            .build();

        // When
        Album savedAlbum = albumRepository.save(album);

        // Then
        assertNotNull(savedAlbum.getId());
        assertEquals("OK Computer", savedAlbum.getName());
        assertEquals(artist.getId(), savedAlbum.getPrimaryArtistId());
    }
}
