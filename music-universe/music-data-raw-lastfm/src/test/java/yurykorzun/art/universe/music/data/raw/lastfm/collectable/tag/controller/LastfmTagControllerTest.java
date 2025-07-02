package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(3L, "electronic"));
        
        when(tagService.findTagsByEntity(entityType, entityId)).thenReturn(tags);

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId);

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
    }

    @Test
    void getEntityTags_shouldReturnEmptyListWhenNoTagsFound() {
        // Given
        Long entityId = 789L;
        LastfmEntityType entityType = LastfmEntityType.ALBUM;
        String entityTypeParam = entityType.getName();
        
        when(tagService.findTagsByEntity(entityType, entityId)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId);

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
            controller.getEntityTags(entityTypeParam, entityId);

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
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(1L, "rock"));
        
        when(tagService.findTagsByEntity(entityType, entityId)).thenReturn(tags);

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId);

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
        
        when(tagService.findTagsByEntity(entityType, entityId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<ResponseWrapper<List<EntityTagDto>>> response =
            controller.getEntityTags(entityTypeParam, entityId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<List<EntityTagDto>> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to fetch entity tags: service error occurred"));
    }
}
