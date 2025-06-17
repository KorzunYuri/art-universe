package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.entity.Artist;
import yurykorzun.art.universe.music.data.approved.entity.Track;

import static org.junit.jupiter.api.Assertions.*;

public class TrackRepositoryTest extends JpaOnlyTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void whenSavingTrack_shouldPersistCorrectly() {
        // Given
        Artist artist = Artist.builder().name("Radiohead").build();
        artistRepository.save(artist);

        Track track = Track.builder()
            .name("Paranoid Android")
            .primaryArtistId(artist.getId())
            .build();

        // When
        Track savedTrack = trackRepository.save(track);

        // Then
        assertNotNull(savedTrack.getId());
        assertEquals("Paranoid Android", savedTrack.getName());
        assertEquals(artist.getId(), savedTrack.getPrimaryArtistId());
        assertNotNull(savedTrack.getCreatedAt());
        assertNotNull(savedTrack.getUpdatedAt());
    }

    @Test
    void whenFindById_shouldReturnCorrectTrack() {
        // Given
        Artist artist = Artist.builder().name("Radiohead").build();
        artistRepository.save(artist);

        Track track = Track.builder()
            .name("Paranoid Android")
            .primaryArtistId(artist.getId())
            .build();
        Track savedTrack = trackRepository.save(track);

        // When
        var foundTrack = trackRepository.findById(savedTrack.getId());

        // Then
        assertTrue(foundTrack.isPresent());
        assertEquals("Paranoid Android", foundTrack.get().getName());
        assertEquals(artist.getId(), foundTrack.get().getPrimaryArtistId());
    }
}
