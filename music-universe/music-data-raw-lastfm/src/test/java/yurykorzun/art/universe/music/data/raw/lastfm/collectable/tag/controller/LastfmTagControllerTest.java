package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmTagControllerTest {

    @Mock
    private LastfmTagService tagService;

    @InjectMocks
    private LastfmTagController controller;

    private void compareDtoAgainstEntity(LastfmTagResponseDto dto, LastfmTag entity) {
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getUrl(), dto.url());
        assertEquals(entity.getUsageCount(), dto.usageCount());
        assertEquals(entity.getUsageUsersCount(), dto.usageUsersCount());
    }

    @Test
    void getTags_shouldReturnDtoPageWrappedInResponse() {
        // given
        String search = "rock";
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        LastfmTag tag1 = LastfmTag.builder()
            .id(1L)
            .name("Rock")
            .url("https://example.com/rock")
            .approvalStatus(ApprovalStatus.APPROVED)
            .usageCount(1000)
            .usageUsersCount(500)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        LastfmTag tag2 = LastfmTag.builder()
            .id(2L)
            .name("Rock Alternative")
            .url("https://example.com/rock-alternative")
            .approvalStatus(ApprovalStatus.PENDING)
            .usageCount(null)
            .usageUsersCount(null)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        List<LastfmTag> tagList = List.of(tag1, tag2);
        Page<LastfmTag> tagPage = new PageImpl<>(tagList, pageable, tagList.size());
        Page<LastfmTagResponseDto> dtoPage = tagPage.map(LastfmTagResponseDto::from);

        TagSearchParams expectedParams = new TagSearchParams(search, approvalStatuses);
        when(tagService.findTags(eq(expectedParams), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        ResponseEntity<ResponseWrapper<Page<LastfmTagResponseDto>>> response = 
            controller.getTags(search, approvalStatuses, pageable);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<Page<LastfmTagResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        Page<LastfmTagResponseDto> data = body.getData();
        assertNotNull(data);
        assertEquals(2, data.getTotalElements());

        for (int i = 0; i < tagList.size(); i++) {
            compareDtoAgainstEntity(data.getContent().get(i), tagList.get(i));
        }
    }

    @Test
    void getTags_shouldHandleNullFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmTag tag = EntityCreationHelper.createTag();
        Page<LastfmTag> tagPage = new PageImpl<>(List.of(tag), pageable, 1);
        Page<LastfmTagResponseDto> dtoPage = tagPage.map(LastfmTagResponseDto::from);
        
        when(tagService.findTags(any(TagSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        ResponseEntity<ResponseWrapper<Page<LastfmTagResponseDto>>> response = 
            controller.getTags(null, null, pageable);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<Page<LastfmTagResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        
        Page<LastfmTagResponseDto> data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.getTotalElements());
    }

    @Test
    void getTags_shouldReturnFailureOnException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(tagService.findTags(any(TagSearchParams.class), any())).thenThrow(new RuntimeException("Fail"));

        // when
        ResponseEntity<ResponseWrapper<Page<LastfmTagResponseDto>>> response = 
            controller.getTags("abc", null, pageable);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<?> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to fetch tags"));
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTag() {
        Long tagId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmTag tag = EntityCreationHelper.createTag(b -> b.approvalStatus(newApprovalStatus));

        when(tagService.updateApprovalStatus(tagId, approvalStatusCode))
            .thenReturn(LastfmTagResponseDto.from(tag));

        ResponseEntity<ResponseWrapper<LastfmTagResponseDto>> response =
            controller.updateApprovalStatus(tagId, new ApprovalStatusRequestDto(approvalStatusCode));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<LastfmTagResponseDto> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        LastfmTagResponseDto data = body.getData();
        assertNotNull(data);
        assertEquals(newApprovalStatus.getCode(), data.approvalStatus());
    }

    @Test
    void updateApprovalStatus_shouldHandleServiceException() {
        Long tagId = 1L;
        when(tagService.updateApprovalStatus(anyLong(), anyInt()))
            .thenThrow(new IllegalArgumentException("Invalid status"));

        ResponseEntity<ResponseWrapper<LastfmTagResponseDto>> response =
            controller.updateApprovalStatus(tagId, new ApprovalStatusRequestDto(999));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<LastfmTagResponseDto> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to update approval status"));
    }
}
