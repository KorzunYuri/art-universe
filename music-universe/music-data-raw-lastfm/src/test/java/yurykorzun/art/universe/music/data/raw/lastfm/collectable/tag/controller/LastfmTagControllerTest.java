package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
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
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        List<EntityTagDto> data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(3L, data.get(0).id());
        assertEquals("electronic", data.get(0).name());
        assertEquals(ApprovalStatus.APPROVED.getCode(), data.get(0).approvalStatus());
        assertEquals(ApprovalStatus.APPROVED.getCode(), data.get(0).tagApprovalStatus());
        assertEquals(ApprovalStatus.PENDING.getCode(), data.get(0).entityApprovalStatus());
        assertEquals(50, data.get(0).usageCount());
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
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        List<EntityTagDto> data = body.getData();
        assertNotNull(data);
        assertEquals(0, data.size());
    }

    @Test
    void getEntityTags_shouldReturnErrorForInvalidEntityTypeName() {
        // Given
        Long entityId = 123L;
        String entityTypeParam = "INVALID";

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response = 
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Invalid entity type: Unknown entity type: INVALID"));
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
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        List<EntityTagDto> data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.size());
    }

    @Test
    void getEntityTags_shouldReturnErrorWhenServiceThrowsException() {
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

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to fetch entity tags: service error occurred"));
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
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId, minUsageCount, approvalStatuses, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
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
            new EntityTagDto(1L, "alternative", ApprovalStatus.APPROVED.getCode(), 
                ApprovalStatus.APPROVED.getCode(), ApprovalStatus.APPROVED.getCode(), 75),
            new EntityTagDto(2L, "rock", ApprovalStatus.APPROVED.getCode(), 
                ApprovalStatus.APPROVED.getCode(), ApprovalStatus.APPROVED.getCode(), 100)
        );
        
        when(tagService.findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            isNull(Pageable.class)
        )).thenReturn(tags);

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tagService).findAllByEntity(
            eq(entityType),
            eq(entityId),
            any(EntityTagSearchParams.class),
            isNull(Pageable.class));
        
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        
        List<EntityTagDto> data = body.getData();
        assertNotNull(data);
        assertEquals(2, data.size());
    }
}
