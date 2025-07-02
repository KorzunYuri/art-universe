package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LastfmTagController.class)
class LastfmTagControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LastfmTagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void GET_entityTags_shouldReturnTagsForValidEntityTypeName() throws Exception {
        // Given
        Long entityId = 456L;
        LastfmEntityType entityType = LastfmEntityType.TRACK;
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(3L, "electronic"));
        
        when(tagService.findTagsByEntity(entityType, entityId)).thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(tags));

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/456", entityType.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_entityTags_shouldReturnEmptyListWhenNoTagsFound() throws Exception {
        // Given
        Long entityId = 789L;
        LastfmEntityType entityType = LastfmEntityType.ALBUM; // code = 2
        
        when(tagService.findTagsByEntity(entityType, entityId)).thenReturn(Collections.emptyList());

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/789", entityType.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_entityTags_shouldReturnError_whenInvalidEntityTypeName() throws Exception {
        // Given
        String errorMessage = "Invalid entity type: Unknown entity type: INVALID. Expected one of: ARTIST, ALBUM, TRACK, TAG or their numeric codes (1, 2, 3, 4)";
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.failureBody(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/INVALID/123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_entityTags_shouldHandleCaseInsensitiveEntityTypeName() throws Exception {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(1L, "rock"));
        
        when(tagService.findTagsByEntity(entityType, entityId)).thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(tags));

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/123", entityType.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_entityTags_shouldReturnError_whenServiceFails() throws Exception {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
        when(tagService.findTagsByEntity(entityType, entityId))
            .thenThrow(new RuntimeException("Database error"));

        String errorMessage = "Failed to fetch entity tags: service error occurred";
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.failureBody(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/123", entityType.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
}
