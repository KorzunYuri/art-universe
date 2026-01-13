package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumControllerTest {

    @Mock
    private LastfmAlbumService albumService;

    @InjectMocks
    private LastfmAlbumController controller;

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedAlbum() {
        Long albumId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmAlbumResponseDto responseDto = new LastfmAlbumResponseDto(
            albumId, "Test Album", "https://example.com/album", "mbid123", 
            approvalStatusCode, 5000L, 1000, null, null);

        when(albumService.updateApprovalStatus(albumId, approvalStatusCode))
            .thenReturn(responseDto);

        LastfmAlbumResponseDto result = controller.updateApprovalStatus(albumId, new ApprovalStatusRequestDto(approvalStatusCode));

        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }
}
