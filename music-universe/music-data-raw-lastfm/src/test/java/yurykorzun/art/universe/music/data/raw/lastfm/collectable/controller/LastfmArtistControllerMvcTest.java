package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller.LastfmArtistController;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup.LastfmArtistLookupService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseMvcTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LastfmArtistController.class)
class LastfmArtistControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmArtistService artistService;

    @MockitoBean
    private LastfmArtistLookupService artistLookupService;
    @MockitoBean
    private LastfmArtistSearchRequestService artistSearchRequestService;
    private List<LastfmArtist> mockArtists;
    private LastfmArtist mockArtist;
    private Pageable defaultPageable = PageRequest.of(0, 20, Sort.by(Sort.Order.asc("name")));

    @BeforeEach
    void setUp() {
        mockArtist = LastfmArtist.builder()
            .id(1L)
            .name("Test Artist")
            .mbid("mbid-123")
            .url("http://test.com")
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(1000L)
            .listenersCount(500)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        LastfmArtist anotherMockedArtist = LastfmArtist.builder()
            .id(2L)
            .name("Another Artist")
            .mbid("mbid-456")
            .url("http://another.com")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(2000L)
            .listenersCount(1000)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        mockArtists = Arrays.asList(
            mockArtist,
            anotherMockedArtist
        );
    }

    @Test
    void GET_artists_shouldReturnArtistsPage() throws Exception {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;

        Page<LastfmArtist> artistsPage = new PageImpl<>(mockArtists, defaultPageable, mockArtists.size());
        Page<LastfmArtistResponseDto> pageResponse = artistsPage.map(LastfmArtistResponseDto::from);

        when(artistService.findAll(any(ArtistSearchParams.class), any(Pageable.class)))
            .thenReturn(pageResponse);

        String expectedJson = objectMapper.writeValueAsString(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .param("search", search)
                .param("minPlayCount", minPlayCount.toString())
                .param("minListenersCount", minListenersCount.toString())
                .param("tagId", "123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_artists_shouldHandleNullFilters() throws Exception {
        // Given
        Page<LastfmArtist> artistsPage = new PageImpl<>(mockArtists, defaultPageable, mockArtists.size());
        Page<LastfmArtistResponseDto> pageResponse = artistsPage.map(LastfmArtistResponseDto::from);

        when(artistService.findAll(any(ArtistSearchParams.class), any(Pageable.class)))
            .thenReturn(pageResponse);

        String expectedJson = objectMapper.writeValueAsString(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_artistById_shouldReturnArtist_whenFound() throws Exception {
        // Given
        Long artistId = 1L;
        LastfmArtistResponseDto responseDto = LastfmArtistResponseDto.from(mockArtist);

        when(artistService.findDtoById(eq(artistId)))
            .thenReturn(responseDto);

        String expectedJson = objectMapper.writeValueAsString(responseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/artists/{id}", artistId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void PATCH_artistApproval_shouldUpdateApprovalStatus_whenValidStatusProvided() throws Exception {
        ApprovalStatusRequestDto request = new ApprovalStatusRequestDto(2);
        LastfmArtistResponseDto responseDto = LastfmArtistResponseDto.from(mockArtist);
        String expectedJson = objectMapper.writeValueAsString(responseDto);

        when(artistService.updateApprovalStatus(mockArtist.getId(), request.approvalStatus()))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/artists/{id}/approval", mockArtist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_lookupArtists_shouldReturnLookupResults() throws Exception {
        // Given
        String search = "Beatles";
        Integer limit = 10;

        List<LookupResultDTO> expectedResults = List.of(
            LookupResultDTO.builder().id(1L).name("The Beatles").build(),
            LookupResultDTO.builder().id(2L).name("Beatles Revival").build()
        );

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();

        when(artistLookupService.lookup(eq(expectedRequest)))
            .thenReturn(expectedResults);

        String expectedJson = objectMapper.writeValueAsString(expectedResults);

        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                .param("search", search)
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_lookupArtists_shouldHandleNullLimit() throws Exception {
        // Given
        String search = "Beatles";

        List<LookupResultDTO> expectedResults = List.of(
            LookupResultDTO.builder().id(1L).name("The Beatles").build()
        );

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(null)
            .build();

        when(artistLookupService.lookup(eq(expectedRequest)))
                .thenReturn(expectedResults);

        String expectedJson = objectMapper.writeValueAsString(expectedResults);

        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                        .param("search", search)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void POST_search_shouldCreateSearchRequest_whenValidRequest() throws Exception {
        // Given
        ArtistSearchRequestDto requestDto = new ArtistSearchRequestDto("Radiohead");
        LastfmArtistSearchRequest savedRequest = LastfmArtistSearchRequest.builder()
            .id(1L)
            .searchString("Radiohead")
            .processed(false)
            .build();

        when(artistSearchRequestService.saveRequest("Radiohead"))
            .thenReturn(savedRequest);

        String expectedJson = objectMapper.writeValueAsString(savedRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/artists/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void POST_search_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Given
        ArtistSearchRequestDto requestDto = new ArtistSearchRequestDto("");

        // When & Then
        mockMvc.perform(post("/api/v1/artists/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void GET_lookupArtists_shouldReturnEmptyList_whenNoMatches() throws Exception {
        // Given
        String search = "NonExistentArtist";
        Integer limit = 10;

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();

        when(artistLookupService.lookup(eq(expectedRequest)))
            .thenReturn(List.of());

        String expectedJson = objectMapper.writeValueAsString(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                .param("search", search)
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
}
