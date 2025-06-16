package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumBindingRepositoryTest extends JpaOnlyTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumBindingRepository albumBindingRepository;

    private Artist artist;
    private Album album1;
    private Album album2;
    private Album album3;

    @BeforeEach
    void setUp() {
        // Create artist
        artist = Artist.builder().name("Radiohead").build();
        artistRepository.save(artist);

        // Create albums
        album1 = Album.builder()
            .name("OK Computer")
            .primaryArtistId(artist.getId())
            .releaseDate(LocalDate.of(1997, 5, 21))
            .build();
        albumRepository.save(album1);

        album2 = Album.builder()
            .name("Kid A")
            .primaryArtistId(artist.getId())
            .releaseDate(LocalDate.of(2000, 10, 2))
            .build();
        albumRepository.save(album2);

        album3 = Album.builder()
            .name("In Rainbows")
            .primaryArtistId(artist.getId())
            .releaseDate(LocalDate.of(2007, 10, 10))
            .build();
        albumRepository.save(album3);

        // Create bindings
        AlbumBinding binding1 = AlbumBinding.builder()
            .referenceId(album1.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(101L)
            .build();
        albumBindingRepository.save(binding1);

        AlbumBinding binding2 = AlbumBinding.builder()
            .referenceId(album2.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(102L)
            .build();
        albumBindingRepository.save(binding2);

        AlbumBinding binding3 = AlbumBinding.builder()
            .referenceId(album3.getId())
            .dataSource(DataSource.MUSICBRAINZ)
            .externalId(103L)
            .build();
        albumBindingRepository.save(binding3);
    }

    @Test
    void whenFindBoundAlbumsForDataSource_shouldReturnCorrectBindings() {
        // When
        List<BoundEntityProjection> bindings = albumBindingRepository.findBoundAlbumsForDataSource(
            DataSource.LASTFM, Arrays.asList(101L, 102L, 999L));

        // Then
        assertEquals(2, bindings.size());
        
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(101L) && 
            b.getDataSource().equals(DataSource.LASTFM) && 
            b.getReferenceId().equals(album1.getId()) && 
            b.getReferenceName().equals("OK Computer")));
            
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(102L) && 
            b.getDataSource().equals(DataSource.LASTFM) && 
            b.getReferenceId().equals(album2.getId()) && 
            b.getReferenceName().equals("Kid A")));
    }

    @Test
    void whenFindBoundAlbumsForDataSource_withDifferentDataSource_shouldReturnCorrectBindings() {
        // When
        List<BoundEntityProjection> bindings = albumBindingRepository.findBoundAlbumsForDataSource(
            DataSource.MUSICBRAINZ, Arrays.asList(103L, 999L));

        // Then
        assertEquals(1, bindings.size());
        
        assertTrue(bindings.stream().anyMatch(b -> 
            b.getExternalId().equals(103L) && 
            b.getDataSource().equals(DataSource.MUSICBRAINZ) && 
            b.getReferenceId().equals(album3.getId()) && 
            b.getReferenceName().equals("In Rainbows")));
    }

    @Test
    void whenFindBoundAlbumsForDataSource_withNonExistingIds_shouldReturnEmptyList() {
        // When
        List<BoundEntityProjection> bindings = albumBindingRepository.findBoundAlbumsForDataSource(
            DataSource.LASTFM, Arrays.asList(999L, 888L));

        // Then
        assertTrue(bindings.isEmpty());
    }
}
