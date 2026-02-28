package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.repository.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelationQueryDispatcherTest {

    @Mock private ArtistTrackRepository artistTrackRepository;
    @Mock private ArtistAlbumRepository artistAlbumRepository;
    @Mock private AlbumTrackRepository albumTrackRepository;
    @Mock private ArtistArtistRepository artistArtistRepository;
    @Mock private AlbumAlbumRepository albumAlbumRepository;
    @Mock private TrackTrackRepository trackTrackRepository;
    @Mock private ArtistCategoryRepository artistCategoryRepository;
    @Mock private AlbumCategoryRepository albumCategoryRepository;
    @Mock private TrackCategoryRepository trackCategoryRepository;

    @InjectMocks
    private RelationQueryDispatcher dispatcher;

    // --- Same-entity relations ---

    @Test
    void findRelatedEntities_artistToArtist_shouldDelegateToArtistArtistRepository() {
        List<RelatedEntityProjection> expected = List.of();
        when(artistArtistRepository.findRelatedArtists(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ARTIST, 1L, MasterEntityType.ARTIST, null);

        assertSame(expected, result);
        verify(artistArtistRepository).findRelatedArtists(1L, null);
    }

    @Test
    void findRelatedEntities_albumToAlbum_shouldDelegateToAlbumAlbumRepository() {
        List<RelatedEntityProjection> expected = List.of();
        when(albumAlbumRepository.findRelatedAlbums(1L, 5L)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ALBUM, 1L, MasterEntityType.ALBUM, 5L);

        assertSame(expected, result);
        verify(albumAlbumRepository).findRelatedAlbums(1L, 5L);
    }

    @Test
    void findRelatedEntities_trackToTrack_shouldDelegateToTrackTrackRepository() {
        List<RelatedEntityProjection> expected = List.of();
        when(trackTrackRepository.findRelatedTracks(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.TRACK, 1L, MasterEntityType.TRACK, null);

        assertSame(expected, result);
        verify(trackTrackRepository).findRelatedTracks(1L, null);
    }

    @Test
    void findRelatedEntities_categoryToCategory_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            dispatcher.findRelatedEntities(MasterEntityType.CATEGORY, 1L, MasterEntityType.CATEGORY, null));
    }

    // --- Cross-entity relations ---

    @Test
    void findRelatedEntities_artistToTrack_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(artistTrackRepository.findRelatedTracksByArtistId(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ARTIST, 1L, MasterEntityType.TRACK, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_trackToArtist_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(artistTrackRepository.findRelatedArtistsByTrackId(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.TRACK, 1L, MasterEntityType.ARTIST, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_artistToAlbum_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(artistAlbumRepository.findRelatedAlbumsByArtistId(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ARTIST, 1L, MasterEntityType.ALBUM, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_albumToArtist_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(artistAlbumRepository.findRelatedArtistsByAlbumId(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ALBUM, 1L, MasterEntityType.ARTIST, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_albumToTrack_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(albumTrackRepository.findRelatedTracksByAlbumId(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ALBUM, 1L, MasterEntityType.TRACK, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_trackToAlbum_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(albumTrackRepository.findRelatedAlbumsByTrackId(1L, null)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.TRACK, 1L, MasterEntityType.ALBUM, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_artistToCategory_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(artistCategoryRepository.findRelatedCategoriesByArtistId(1L)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ARTIST, 1L, MasterEntityType.CATEGORY, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_albumToCategory_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(albumCategoryRepository.findRelatedCategoriesByAlbumId(1L)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.ALBUM, 1L, MasterEntityType.CATEGORY, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_trackToCategory_shouldDelegate() {
        List<RelatedEntityProjection> expected = List.of();
        when(trackCategoryRepository.findRelatedCategoriesByTrackId(1L)).thenReturn(expected);

        List<RelatedEntityProjection> result = dispatcher.findRelatedEntities(
            MasterEntityType.TRACK, 1L, MasterEntityType.CATEGORY, null);

        assertSame(expected, result);
    }

    @Test
    void findRelatedEntities_unsupportedCrossEntity_shouldThrow() {
        // category -> artist is not supported
        assertThrows(IllegalArgumentException.class, () ->
            dispatcher.findRelatedEntities(MasterEntityType.CATEGORY, 1L, MasterEntityType.ARTIST, null));
    }
}
