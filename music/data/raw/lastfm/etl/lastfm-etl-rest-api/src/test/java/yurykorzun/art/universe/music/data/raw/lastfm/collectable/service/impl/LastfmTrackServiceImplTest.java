package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTrackServiceImplTest {
    
    @Mock
    private LastfmTrackRepository trackRepository;

    @InjectMocks
    private LastfmTrackServiceImpl trackService;

    private LastfmTrack createTrack(Consumer<LastfmTrack.LastfmTrackBuilder<?,?>> customizer) {
        return EntityCreationHelper.createTrack(customizer);
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTrack() {
        long trackId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist artist = EntityCreationHelper.createArtist();
        LastfmTrack existing = createTrack(b -> b.id(trackId).approvalStatus(oldStatus).artist(artist));
        LastfmTrack updated = createTrack(b -> b.id(trackId).approvalStatus(newStatus).artist(artist));

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(existing));
        when(trackRepository.save(any(LastfmTrack.class))).thenReturn(updated);

        LastfmTrackResponseDto result = trackService.updateApprovalStatus(trackId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        assertNotNull(result.artist());
        assertEquals(artist.getId(), result.artist().id());
        verify(trackRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingTrack_shouldThrowException() {
        when(trackRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> trackService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> trackService.updateApprovalStatus(1L, -1)
        );
    }
}
