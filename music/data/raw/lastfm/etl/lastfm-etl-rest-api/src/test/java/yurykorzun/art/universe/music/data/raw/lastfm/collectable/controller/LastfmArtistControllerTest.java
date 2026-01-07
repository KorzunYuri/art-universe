package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmArtistControllerTest {

    @Mock
    private LastfmArtistService artistService;

    @Mock
    private LastfmArtistSearchRequestService artistSearchRequestService;

    @InjectMocks
    private LastfmArtistController controller;

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedArtist() {
        // given
        Long artistId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmArtistResponseDto responseDto = new LastfmArtistResponseDto(
            artistId, "Test Artist", "https://example.com/artist", "mbid123", 
            approvalStatusCode, 5000L, 1000);

        when(artistService.updateApprovalStatus(artistId, approvalStatusCode))
            .thenReturn(responseDto);

        // when
        LastfmArtistResponseDto result = controller.updateApprovalStatus(artistId, new ApprovalStatusRequestDto(approvalStatusCode));

        // then
        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }

    @Test
    void createSearchRequest_shouldReturnSavedRequest_whenValidRequest() {
        // given
        ArtistSearchRequestDto requestDto = new ArtistSearchRequestDto("Radiohead");
        LastfmArtistSearchRequest savedRequest = LastfmArtistSearchRequest.builder()
                .id(1L)
                .searchString("Radiohead")
                .processed(false)
                .build();

        when(artistSearchRequestService.saveRequest("Radiohead"))
                .thenReturn(savedRequest);

        // when
        LastfmArtistSearchRequest result = controller.createSearchRequest(requestDto);

        // then
        assertNotNull(result);
        assertEquals(savedRequest, result);
        assertEquals("Radiohead", result.getSearchString());
        assertFalse(result.isProcessed());
    }
}
