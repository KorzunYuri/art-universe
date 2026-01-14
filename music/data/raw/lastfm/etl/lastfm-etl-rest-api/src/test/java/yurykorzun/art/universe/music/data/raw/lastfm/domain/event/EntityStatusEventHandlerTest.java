package yurykorzun.art.universe.music.data.raw.lastfm.domain.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityStatusEventHandlerTest {

    @Mock
    private LastfmTrackRepository trackRepository;

    @Mock
    private LastfmAlbumRepository albumRepository;

    @InjectMocks
    private EntityStatusEventHandler handler;

    @Test
    void handleEntityStatusChanged_shouldUpdateTracksAndAlbumsForDeclinedArtist() {
        // Given
        Long artistId = 1L;
        EntityStatusChangedEvent event = new EntityStatusChangedEvent(
            LastfmEntityType.ARTIST, artistId, ApprovalStatus.DECLINED
        );
        when(trackRepository.updateTrackStatusByArtistId(artistId, ApprovalStatus.DECLINED)).thenReturn(5);
        when(albumRepository.updateAlbumStatusByArtistId(artistId, ApprovalStatus.DECLINED)).thenReturn(3);

        // When
        handler.handleEntityStatusChanged(event);

        // Then
        verify(trackRepository).updateTrackStatusByArtistId(artistId, ApprovalStatus.DECLINED);
        verify(albumRepository).updateAlbumStatusByArtistId(artistId, ApprovalStatus.DECLINED);
    }

    @Test
    void handleEntityStatusChanged_shouldUpdateTracksForIgnoredAlbum() {
        // Given
        Long albumId = 2L;
        EntityStatusChangedEvent event = new EntityStatusChangedEvent(
            LastfmEntityType.ALBUM, albumId, ApprovalStatus.IGNORED
        );
        when(trackRepository.updateTrackStatusByAlbumId(albumId, ApprovalStatus.IGNORED)).thenReturn(2);

        // When
        handler.handleEntityStatusChanged(event);

        // Then
        verify(trackRepository).updateTrackStatusByAlbumId(albumId, ApprovalStatus.IGNORED);
        verifyNoInteractions(albumRepository);
    }

    @Test
    void handleEntityStatusChanged_shouldIgnoreApprovedStatus() {
        // Given
        EntityStatusChangedEvent event = new EntityStatusChangedEvent(
            LastfmEntityType.ARTIST, 1L, ApprovalStatus.APPROVED
        );

        // When
        handler.handleEntityStatusChanged(event);

        // Then
        verifyNoInteractions(trackRepository, albumRepository);
    }

    @Test
    void handleEntityStatusChanged_shouldIgnorePendingStatus() {
        // Given
        EntityStatusChangedEvent event = new EntityStatusChangedEvent(
            LastfmEntityType.ALBUM, 1L, ApprovalStatus.PENDING
        );

        // When
        handler.handleEntityStatusChanged(event);

        // Then
        verifyNoInteractions(trackRepository, albumRepository);
    }

    @Test
    void handleEntityStatusChanged_shouldIgnoreUnsupportedEntityType() {
        // Given
        EntityStatusChangedEvent event = new EntityStatusChangedEvent(
            LastfmEntityType.TAG, 1L, ApprovalStatus.DECLINED
        );

        // When
        handler.handleEntityStatusChanged(event);

        // Then
        verifyNoInteractions(trackRepository, albumRepository);
    }
}
