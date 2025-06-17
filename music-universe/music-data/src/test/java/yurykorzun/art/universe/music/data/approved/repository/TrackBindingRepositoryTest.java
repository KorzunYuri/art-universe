package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackBindingRepositoryTest extends JpaOnlyTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TrackBindingRepository trackBindingRepository;

    private Artist artist;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        // Create artist
        artist = Artist.builder().name("Radiohead").build();
        artistRepository.save(artist);

        // Create tracks
        track1 = Track.builder()
            .name("Paranoid Android")
            .primaryArtistId(artist.getId())
            .build();
        trackRepository.save(track1);

        track2 = Track.builder()
            .name("Karma Police")
            .primaryArtistId(artist.getId())
            .build();
        trackRepository.save(track2);

        track3 = Track.builder()
            .name("No Surprises")
            .primaryArtistId(artist.getId())
            .build();
        trackRepository.save(track3);

        // Create bindings
        TrackBinding binding1 = TrackBinding.builder()
            .referenceId(track1.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(101L)
            .build();
        trackBindingRepository.save(binding1);

        TrackBinding binding2 = TrackBinding.builder()
            .referenceId(track2.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(102L)
            .build();
        trackBindingRepository.save(binding2);

        TrackBinding binding3 = TrackBinding.builder()
            .referenceId(track3.getId())
            .dataSource(DataSource.MUSICBRAINZ)
            .externalId(103L)
            .build();
        trackBindingRepository.save(binding3);
    }

    @Test
    void whenFindBoundTracksForDataSource_shouldReturnCorrectBindings() {
        // When
        List<BoundEntityProjection> bindings = trackBindingRepository.findBoundTracksForDataSource(
            DataSource.LASTFM, Arrays.asList(101L, 102L, 999L));

        // Then
        assertEquals(2, bindings.size());
        
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(101L) && 
            b.getDataSource().equals(DataSource.LASTFM) && 
            b.getReferenceId().equals(track1.getId()) && 
            b.getReferenceName().equals("Paranoid Android")));
            
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(102L) && 
            b.getDataSource().equals(DataSource.LASTFM) && 
            b.getReferenceId().equals(track2.getId()) && 
            b.getReferenceName().equals("Karma Police")));
    }

    @Test
    void whenFindBoundTracksForDataSource_withDifferentDataSource_shouldReturnCorrectBindings() {
        // When
        List<BoundEntityProjection> bindings = trackBindingRepository.findBoundTracksForDataSource(
            DataSource.MUSICBRAINZ, Arrays.asList(103L, 999L));

        // Then
        assertEquals(1, bindings.size());
        
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(103L) && 
            b.getDataSource().equals(DataSource.MUSICBRAINZ) && 
            b.getReferenceId().equals(track3.getId()) && 
            b.getReferenceName().equals("No Surprises")));
    }

    @Test
    void whenFindBoundTracksForDataSource_withNonExistingIds_shouldReturnEmptyList() {
        // When
        List<BoundEntityProjection> bindings = trackBindingRepository.findBoundTracksForDataSource(
            DataSource.LASTFM, Arrays.asList(999L, 888L));

        // Then
        assertTrue(bindings.isEmpty());
    }
}
