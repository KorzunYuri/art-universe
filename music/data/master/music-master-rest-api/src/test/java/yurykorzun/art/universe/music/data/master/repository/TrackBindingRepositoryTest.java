package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.music.data.master.entity.TrackBinding;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackBindingRepositoryTest extends BaseMasterDataJpaTest {

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
            .masterId(track1.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(101L)
            .build();
        trackBindingRepository.save(binding1);

        TrackBinding binding2 = TrackBinding.builder()
            .masterId(track2.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(102L)
            .build();
        trackBindingRepository.save(binding2);

        TrackBinding binding3 = TrackBinding.builder()
            .masterId(track3.getId())
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
            b.getMasterId().equals(track1.getId()) &&
            b.getMasterName().equals("Paranoid Android")));
            
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(102L) && 
            b.getDataSource().equals(DataSource.LASTFM) && 
            b.getMasterId().equals(track2.getId()) &&
            b.getMasterName().equals("Karma Police")));
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
            b.getMasterId().equals(track3.getId()) &&
            b.getMasterName().equals("No Surprises")));
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
