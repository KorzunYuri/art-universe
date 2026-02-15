package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.lookup.LastfmTagLookupService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LastfmTagController.class)
class LastfmTagControllerMvcTest extends BaseMvcTest {

    @MockitoBean
    private LastfmTagService tagService;

    @MockitoBean
    private LastfmTagLookupService tagLookupService;

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
        
        when(tagService.findById(eq(tagId)))
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
    
    @Test
    void GET_tags_shouldAcceptMinUsageCountParameter() throws Exception {
        // Given
        Integer minUsageCount = 100;
        
        List<LastfmTag> tags = List.of(
            LastfmTag.builder()
                .id(1L)
                .name("rock")
                .url("https://example.com/rock")
                .usageCount(150)
                .usageUsersCount(75)
                .approvalStatus(ApprovalStatus.APPROVED)
                .apiCall(EntityCreationHelper.createApiCall())
                .build()
        );
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
        Page<LastfmTag> tagPage = new PageImpl<>(tags, pageable, tags.size());
        Page<LastfmTagResponseDto> dtoPage = tagPage.map(LastfmTagResponseDto::from);
        
        when(tagService.findAll(any(TagSearchParams.class), any(Pageable.class)))
            .thenReturn(dtoPage);

        String expectedJson = objectMapper.writeValueAsString(dtoPage);

        // When & Then
        mockMvc.perform(get("/api/v1/tags")
                .param("minUsageCount", minUsageCount.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
    
    @Test
    void GET_tags_shouldAcceptMinUsageUsersCountParameter() throws Exception {
        // Given
        Integer minUsageUsersCount = 50;
        
        List<LastfmTag> tags = List.of(
            LastfmTag.builder()
                .id(1L)
                .name("pop")
                .url("https://example.com/pop")
                .usageCount(200)
                .usageUsersCount(100)
                .approvalStatus(ApprovalStatus.APPROVED)
                .apiCall(EntityCreationHelper.createApiCall())
                .build()
        );
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
        Page<LastfmTag> tagPage = new PageImpl<>(tags, pageable, tags.size());
        Page<LastfmTagResponseDto> dtoPage = tagPage.map(LastfmTagResponseDto::from);
        
        when(tagService.findAll(any(TagSearchParams.class), any(Pageable.class)))
            .thenReturn(dtoPage);

        String expectedJson = objectMapper.writeValueAsString(dtoPage);

        // When & Then
        mockMvc.perform(get("/api/v1/tags")
                .param("minUsageUsersCount", minUsageUsersCount.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
    
    @Test
    void GET_lookupTags_shouldReturnLookupResults() throws Exception {
        // Given
        String search = "rock";
        Integer limit = 10;
        
        List<LookupResultDTO> expectedResults = List.of(
            LookupResultDTO.builder().id(1L).name("rock").build(),
            LookupResultDTO.builder().id(2L).name("rock music").build()
        );

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();

        when(tagLookupService.lookup(eq(expectedRequest)))
            .thenReturn(expectedResults);

        String expectedJson = objectMapper.writeValueAsString(expectedResults);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/lookup")
                .param("search", search)
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_lookupTags_shouldHandleNullLimit() throws Exception {
        // Given
        String search = "jazz";
        
        List<LookupResultDTO> expectedResults = List.of(
            LookupResultDTO.builder().id(1L).name("jazz").build()
        );

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(null)
            .build();

        when(tagLookupService.lookup(eq(expectedRequest)))
            .thenReturn(expectedResults);

        String expectedJson = objectMapper.writeValueAsString(expectedResults);

        // When & Then
        mockMvc.perform(get("/api/v1/tags/lookup")
                .param("search", search)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_lookupTags_shouldReturnEmptyList_whenNoMatches() throws Exception {
        // Given
        String search = "NonExistentTag";
        Integer limit = 10;
        
        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();

        when(tagLookupService.lookup(eq(expectedRequest)))
            .thenReturn(List.of());

        String expectedJson = objectMapper.writeValueAsString(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/tags/lookup")
                .param("search", search)
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
}
