package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.common.exception.DataFetchException;
import yurykorzun.art.universe.common.exception.ValidationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        LastfmTag tag = LastfmTag.builder()
            .id(tagId)
            .name("rock")
            .url("https://example.com/tag/rock")
            .usageCount(5000)
            .usageUsersCount(1000)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        when(tagService.findById(tagId)).thenReturn(Optional.of(tag));

        // When
        LastfmTagResponseDto response = controller.getTagById(tagId);

        // Then
        assertNotNull(response);
        assertEquals(tagId, response.id());
        assertEquals("rock", response.name());
        assertEquals("https://example.com/tag/rock", response.url());
        assertEquals(ApprovalStatus.APPROVED.getCode(), response.approvalStatus());
        assertEquals(5000, response.usageCount());
        assertEquals(1000, response.usageUsersCount());
    }

    @Test
    void getTagById_shouldThrowEntityNotFoundExceptionWhenTagDoesNotExist() {
        // Given
        Long tagId = 999L;
        when(tagService.findById(tagId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            controller.getTagById(tagId);
        });
        
        assertTrue(exception.getMessage().contains("Tag not found"));
    }

    @Test
    void getTagById_shouldThrowDataFetchExceptionWhenExceptionOccurs() {
        // Given
        Long tagId = 1L;
        when(tagService.findById(tagId)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        DataFetchException exception = assertThrows(DataFetchException.class, () -> {
            controller.getTagById(tagId);
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch tag"));
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
        
        // Use isNull() instead of any() for null parameters
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            isNull(Pageable.class)
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
            isNull(Pageable.class)
        )).thenReturn(Collections.emptyList());

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void getEntityTags_shouldThrowValidationExceptionForInvalidEntityTypeName() {
        // Given
        Long entityId = 123L;
        String entityTypeParam = "INVALID";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);
        });
        
        assertTrue(exception.getMessage().contains("Invalid entity type"));
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
            isNull(Pageable.class)
        )).thenReturn(tags);

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getEntityTags_shouldThrowDataFetchExceptionWhenServiceThrowsException() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        String entityTypeParam = entityType.getName();
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            isNull(Pageable.class)
        )).thenThrow(new RuntimeException("Database error"));

        // When & Then
        DataFetchException exception = assertThrows(DataFetchException.class, () -> {
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch entity tags"));
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
            isNull(Pageable.class)
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
            isNull(Pageable.class));
        
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
            isNull(Pageable.class)
        )).thenReturn(tags);

        // When
        List<EntityTagDto> response = controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        verify(tagService).findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            isNull(Pageable.class));
        
        assertNotNull(response);
        assertEquals(2, response.size());
    }
}
