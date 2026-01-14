package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmTagControllerTest {

    @Mock
    private LastfmTagService tagService;

    @InjectMocks
    private LastfmTagController controller;

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTag() {
        // Given
        Long tagId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmTagResponseDto responseDto = new LastfmTagResponseDto(
            tagId, "rock", "https://example.com/tag/rock", 
            approvalStatusCode, 5000, 1000);

        when(tagService.updateApprovalStatus(tagId, approvalStatusCode))
            .thenReturn(responseDto);

        // When
        LastfmTagResponseDto result = controller.updateApprovalStatus(tagId, new ApprovalStatusRequestDto(approvalStatusCode));

        // Then
        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }
}
