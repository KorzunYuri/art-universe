package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseMvcTest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LastfmTagController.class)
class LastfmTagControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LastfmTagService tagService;

    @Autowired
    private ObjectMapper objectMapper;
    
    private LastfmTag mockTag;
    
    @BeforeEach
    void setUp() {
        mockTag = LastfmTag.builder()
            .id(1L)
            .name("rock")
            .url("https://example.com/tag/rock")
            .usageCount(5000)
            .usageUsersCount(1000)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();
    }

    @Test
    void GET_tagById_shouldReturnTag_whenFound() throws Exception {
        // Given
        Long tagId = 1L;
        LastfmTagResponseDto responseDto = LastfmTagResponseDto.from(mockTag);
        
        when(tagService.findDtoById(eq(tagId)))
            .thenReturn(responseDto);

        String expectedJson = objectMapper.writeValueAsString(responseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_entityTags_shouldReturnTagsForValidEntityTypeName() throws Exception {
        // Given
        Long entityId = 456L;
        LastfmEntityType entityType = LastfmEntityType.TRACK;
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(
            3L, 
            "electronic", 
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.PENDING.getCode(),
            50
        ));
        
        when(tagService.findAllByEntity(eq(entityType), eq(entityId), any(EntityTagSearchParams.class), any(Pageable.class)))
            .thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(tags);

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
        
        when(tagService.findAllByEntity(eq(entityType), eq(entityId), any(EntityTagSearchParams.class), any(Pageable.class)))
            .thenReturn(Collections.emptyList());

        String expectedJson = objectMapper.writeValueAsString(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/789", entityType.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_entityTags_shouldHandleCaseInsensitiveEntityTypeName() throws Exception {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
        List<EntityTagDto> tags = List.of(new EntityTagDto(
            1L, 
            "rock", 
            ApprovalStatus.PENDING.getCode(),
            ApprovalStatus.PENDING.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            100
        ));
        
        when(tagService.findAllByEntity(eq(entityType), eq(entityId), any(EntityTagSearchParams.class), any(Pageable.class)))
            .thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(tags);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/123", entityType.getName().toLowerCase())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
    
    @Test
    void GET_entityTags_shouldAcceptMinUsageCountParameter() throws Exception {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        Integer minUsageCount = 50;
        
        List<EntityTagDto> tags = List.of(
            new EntityTagDto(1L,
                "rock",
                ApprovalStatus.PENDING.getCode(),
                ApprovalStatus.PENDING.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                100),
            new EntityTagDto(2L,
                "pop",
                ApprovalStatus.PENDING.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                ApprovalStatus.APPROVED.getCode(),
                75)
        );
        
        when(tagService.findAllByEntity(eq(entityType), eq(entityId), any(EntityTagSearchParams.class), any(Pageable.class)))
            .thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(tags);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/{entityId}", entityType.getName(), entityId)
                .param("minUsageCount", minUsageCount.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
    
    @Test
    void GET_entityTags_shouldAcceptApprovalStatusesParameter() throws Exception {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        Set<Integer> approvalStatuses = Set.of(ApprovalStatus.APPROVED.getCode());
        
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
        
        when(tagService.findAllByEntity(eq(entityType), eq(entityId), any(EntityTagSearchParams.class), any(Pageable.class)))
            .thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(tags);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/{entityId}", entityType.getName(), entityId)
                .param("approvalStatuses", String.valueOf(ApprovalStatus.APPROVED.getCode()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
    
    @Test
    void GET_entityTags_shouldAcceptPaginationParameters() throws Exception {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
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
        
        when(tagService.findAllByEntity(eq(entityType), eq(entityId), any(EntityTagSearchParams.class), any(Pageable.class)))
            .thenReturn(tags);

        String expectedJson = objectMapper.writeValueAsString(tags);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/entity/{entityType}/{entityId}", entityType.getName(), entityId)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
}
