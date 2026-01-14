package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmTrackControllerTest {

    @Mock
    private LastfmTrackService trackService;

    @InjectMocks
    private LastfmTrackController controller;

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTrack() {
        Long trackId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmTrackResponseDto responseDto = new LastfmTrackResponseDto(
            trackId, "Test Track", "https://example.com/track", "mbid123", 
            approvalStatusCode, 1000, 5000L, 
            new LastfmArtistResponseDto(
                2L, "Test Artist", "https://example.com/artist", "artist-mbid", 
                ApprovalStatus.APPROVED.getCode(), 10000L, 5000));

        when(trackService.updateApprovalStatus(trackId, approvalStatusCode))
            .thenReturn(responseDto);

        LastfmTrackResponseDto result = controller.updateApprovalStatus(trackId, new ApprovalStatusRequestDto(approvalStatusCode));

        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }
}
