package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.event.EntityStatusChangedEvent;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumServiceImplTest {
    
    @Mock
    private LastfmAlbumRepository albumRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LastfmAlbumServiceImpl albumService;

    private LastfmAlbum createAlbum() {
        return EntityCreationHelper.createAlbum();
    }

    private LastfmAlbum createAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?,?>> customizer) {
        return EntityCreationHelper.createAlbum(customizer);
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedAlbum() {
        long albumId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist artist = EntityCreationHelper.createArtist();
        LastfmAlbum existing = createAlbum(b -> b.id(albumId).approvalStatus(oldStatus).artist(artist));
        LastfmAlbum updated = createAlbum(b -> b.id(albumId).approvalStatus(newStatus).artist(artist));

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existing));
        when(albumRepository.save(any(LastfmAlbum.class))).thenReturn(updated);

        LastfmAlbumResponseDto result = albumService.updateApprovalStatus(albumId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        assertNotNull(result.artist());
        assertEquals(artist.getId(), result.artist().id());
        verify(albumRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingAlbum_shouldThrowException() {
        when(albumRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> albumService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> albumService.updateApprovalStatus(1L, -1)
        );
    }

    @Test
    void updateApprovalStatus_shouldPublishEvent() {
        // Given
        Long albumId = 1L;
        LastfmAlbum album = LastfmAlbum.builder()
                .name("Test Album")
                .url("http://test.com")
                .approvalStatus(ApprovalStatus.PENDING)
                .apiCall(EntityCreationHelper.createApiCall())
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumRepository.save(any(LastfmAlbum.class))).thenReturn(album);

        // When
        albumService.updateApprovalStatus(albumId, ApprovalStatus.IGNORED.getCode());

        // Then
        ArgumentCaptor<EntityStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(EntityStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityStatusChangedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(LastfmEntityType.ALBUM, publishedEvent.entityType());
        assertEquals(albumId, publishedEvent.entityId());
        assertEquals(ApprovalStatus.IGNORED, publishedEvent.newStatus());
    }

    @Test
    void updateApprovalStatus_shouldPublishEventForAnyStatus() {
        // Given
        Long albumId = 1L;
        LastfmAlbum album = LastfmAlbum.builder()
                .name("Test Album")
                .url("http://test.com")
                .apiCall(EntityCreationHelper.createApiCall())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumRepository.save(any(LastfmAlbum.class))).thenReturn(album);

        // When
        albumService.updateApprovalStatus(albumId, ApprovalStatus.APPROVED.getCode());

        // Then
        ArgumentCaptor<EntityStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(EntityStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityStatusChangedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(ApprovalStatus.APPROVED, publishedEvent.newStatus());
    }
}
