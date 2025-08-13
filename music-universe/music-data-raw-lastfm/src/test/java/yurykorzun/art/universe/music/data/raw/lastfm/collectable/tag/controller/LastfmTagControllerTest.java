package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTagControllerTest {

    @Mock
    private LastfmTagService tagService;

    @InjectMocks
    private LastfmTagController controller;

    @Test
    void getTagById_shouldReturnTagWhenFound() {
        // Given
        Long tagId = 1L;
        LastfmTagResponseDto responseDto = new LastfmTagResponseDto(
            tagId, "rock", "https://example.com/tag/rock", 
            ApprovalStatus.APPROVED.getCode(), 5000, 1000);

        when(tagService.findDtoById(tagId)).thenReturn(responseDto);

        // When
        LastfmTagResponseDto result = controller.getTagById(tagId);

        // Then
        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void getTags_shouldReturnDtoPage() {
        // Given
        String search = "rock";
        Set<Integer> approvalStatuses = Set.of(ApprovalStatus.APPROVED.getCode());
        Integer minUsageCount = 100;
        Integer minUsageUsersCount = 50;
        Pageable pageable = PageRequest.of(0, 2);

        LastfmTag tag1 = LastfmTag.builder()
            .id(1L)
            .name("rock")
            .url("https://example.com/rock")
            .approvalStatus(ApprovalStatus.APPROVED)
            .usageCount(5000)
            .usageUsersCount(1000)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        LastfmTag tag2 = LastfmTag.builder()
            .id(2L)
            .name("alternative rock")
            .url("https://example.com/alternative-rock")
            .approvalStatus(ApprovalStatus.PENDING)
            .usageCount(null)
            .usageUsersCount(null)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        List<LastfmTag> tagList = List.of(tag1, tag2);
        Page<LastfmTag> tagPage = new PageImpl<>(tagList, pageable, tagList.size());
        Page<LastfmTagResponseDto> dtoPage = tagPage.map(LastfmTagResponseDto::from);

        when(tagService.findAll(any(TagSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // When
        Page<LastfmTagResponseDto> result = controller.getTags(search, approvalStatuses, minUsageCount, minUsageUsersCount, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        for (int i = 0; i < tagList.size(); i++) {
            LastfmTagResponseDto dto = result.getContent().get(i);
            LastfmTag entity = tagList.get(i);
            
            assertEquals(entity.getId(), dto.id());
            assertEquals(entity.getName(), dto.name());
            assertEquals(entity.getUrl(), dto.url());
            assertEquals(entity.getApprovalStatus().getCode(), dto.approvalStatus());
            assertEquals(entity.getUsageCount(), dto.usageCount());
            assertEquals(entity.getUsageUsersCount(), dto.usageUsersCount());
        }
    }
    
    @Test
    void getTags_shouldHandleNullFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmTag tag = LastfmTag.builder()
            .id(1L)
            .name("test")
            .approvalStatus(ApprovalStatus.PENDING)
            .apiCall(mock(LastfmApiCall.class))
            .build();
        
        Page<LastfmTag> tagPage = new PageImpl<>(List.of(tag), pageable, 1);
        Page<LastfmTagResponseDto> dtoPage = tagPage.map(LastfmTagResponseDto::from);
        
        when(tagService.findAll(any(TagSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // When
        Page<LastfmTagResponseDto> result = controller.getTags(null, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

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

    @Test
    void getEntityTags_shouldReturnTagsForValidEntityTypeName() {
        // Given
        Long entityId = 456L;
        LastfmEntityType entityType = LastfmEntityType.TRACK;
        String entityTypeParam = entityType.getName();
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(
            3L, 
            "electronic", 
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.PENDING.getCode(),
            50
        ));
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            nullable(Pageable.class)
        )).thenReturn(tags);

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(3L, response.get(0).id());
        assertEquals("electronic", response.get(0).name());
        assertEquals(ApprovalStatus.APPROVED.getCode(), response.get(0).relationApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED.getCode(), response.get(0).tagApprovalStatus());
        assertEquals(ApprovalStatus.PENDING.getCode(), response.get(0).entityApprovalStatus());
        assertEquals(50, response.get(0).usageCount());
    }

    @Test
    void getEntityTags_shouldReturnEmptyListWhenNoTagsFound() {
        // Given
        Long entityId = 789L;
        LastfmEntityType entityType = LastfmEntityType.ALBUM;
        String entityTypeParam = entityType.getName();
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            nullable(Pageable.class)
        )).thenReturn(Collections.emptyList());

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void getEntityTags_shouldThrowIllegalArgumentExceptionForInvalidEntityTypeName() {
        // Given
        Long entityId = 123L;
        String entityTypeParam = "INVALID";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);
        });
        
        assertTrue(exception.getMessage().contains("Unknown entity type"));
    }

    @Test
    void getEntityTags_shouldHandleCaseInsensitiveEntityTypeName() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        String entityTypeParam = entityType.getName().toLowerCase();
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(
            1L, 
            "rock", 
            ApprovalStatus.PENDING.getCode(),
            ApprovalStatus.PENDING.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            100
        ));
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            nullable(Pageable.class)
        )).thenReturn(tags);

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
    }
    
    @Test
    void getEntityTags_shouldPassSearchParamsToService() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        String entityTypeParam = entityType.getName();
        Integer minUsageCount = 50;
        Set<Integer> approvalStatuses = Set.of(ApprovalStatus.APPROVED.getCode());
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(
            1L, 
            "rock", 
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            100
        ));
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            nullable(Pageable.class)
        )).thenReturn(tags);

        // When
        controller.getEntityTags(entityTypeParam, entityId, minUsageCount, approvalStatuses, null);

        // Then
        // Capture and verify the search params
        ArgumentCaptor<EntityTagSearchParams> searchParamsCaptor = ArgumentCaptor.forClass(EntityTagSearchParams.class);
        verify(tagService).findAllByEntity(
            eq(entityType),
            eq(entityId),
            searchParamsCaptor.capture(),
            isNull());
        
        EntityTagSearchParams capturedParams = searchParamsCaptor.getValue();
        assertEquals(minUsageCount, capturedParams.minUsageCount());
        assertEquals(approvalStatuses, capturedParams.approvalStatuses());
    }
    
    @Test
    void getEntityTags_shouldPassPageableToService() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        String entityTypeParam = entityType.getName();
        Pageable pageable = PageRequest.of(0, 10);
        
        List<EntityTagDto> tags = List.of(
            new EntityTagDto(1L,
                "alternative",
                ApprovalStatus.APPROVED.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                75),
            new EntityTagDto(2L,
                "rock",
                ApprovalStatus.APPROVED.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                100)
        );
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            eq(pageable)
        )).thenReturn(tags);

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, pageable);

        // Then
        verify(tagService).findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            eq(pageable));
        
        assertNotNull(response);
        assertEquals(2, response.size());
    }
}
