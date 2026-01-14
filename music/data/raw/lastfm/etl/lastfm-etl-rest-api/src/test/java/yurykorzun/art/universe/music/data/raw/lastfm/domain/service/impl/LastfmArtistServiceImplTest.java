package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.event.EntityStatusChangedEvent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistServiceImplTest {
    
    @Mock
    private LastfmArtistRepository artistRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LastfmArtistServiceImpl artistService;

    private LastfmArtist createArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?,?>> overrideDefaults) {
        LastfmArtist.LastfmArtistBuilder<?, ?> builder = LastfmArtist.builder()
            .id(1L)
            .name("Test Artist")
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(mock(LastfmApiCall.class));
        overrideDefaults.accept(builder);
        return builder.build();
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedArtist() {
        long artistId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist existing = createArtist(b -> b.approvalStatus(oldStatus));
        LastfmArtist updated = createArtist(b -> b.approvalStatus(newStatus));

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(existing));
        when(artistRepository.save(any(LastfmArtist.class))).thenReturn(updated);

        LastfmArtistResponseDto result = artistService.updateApprovalStatus(artistId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        verify(artistRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingArtist_shouldThrowException() {
        when(artistRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> artistService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> artistService.updateApprovalStatus(1L, -1)
        );
    }

    @Test
    void updateApprovalStatus_shouldPublishEvent() {
        // Given
        Long artistId = 1L;
        LastfmArtist artist = LastfmArtist.builder()
                .name("Test Artist")
                .approvalStatus(ApprovalStatus.PENDING)
                .apiCall(EntityCreationHelper.createApiCall())
                .build();

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistRepository.save(any(LastfmArtist.class))).thenReturn(artist);

        // When
        artistService.updateApprovalStatus(artistId, ApprovalStatus.DECLINED.getCode());

        // Then
        ArgumentCaptor<EntityStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(EntityStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityStatusChangedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(LastfmEntityType.ARTIST, publishedEvent.entityType());
        assertEquals(artistId, publishedEvent.entityId());
        assertEquals(ApprovalStatus.DECLINED, publishedEvent.newStatus());
    }

    @Test
    void updateApprovalStatus_shouldPublishEventForAnyStatus() {
        // Given
        Long artistId = 1L;
        LastfmArtist artist = LastfmArtist.builder()
                .name("Test Artist")
                .approvalStatus(ApprovalStatus.PENDING)
                .apiCall(EntityCreationHelper.createApiCall())
                .build();

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistRepository.save(any(LastfmArtist.class))).thenReturn(artist);

        // When
        artistService.updateApprovalStatus(artistId, ApprovalStatus.APPROVED.getCode());

        // Then
        ArgumentCaptor<EntityStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(EntityStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityStatusChangedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(ApprovalStatus.APPROVED, publishedEvent.newStatus());
    }
}
