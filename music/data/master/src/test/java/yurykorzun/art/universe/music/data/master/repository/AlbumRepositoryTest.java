package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.master.common.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.entity.Album;
import yurykorzun.art.universe.music.data.master.entity.Artist;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumRepositoryTest extends BaseMasterDataJpaTest {

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
