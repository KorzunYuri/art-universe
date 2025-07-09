package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LastfmArtistController.class)
class LastfmArtistControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmArtistService artistService;

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
            .playCount(1000)
            .listenersCount(500)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        LastfmArtist anotherMockedArtist = LastfmArtist.builder()
            .id(2L)
            .name("Another Artist")
            .mbid("mbid-456")
            .url("http://another.com")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(2000)
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

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(pageResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .param("search", search)
                .param("minPlayCount", minPlayCount.toString())
                .param("minListenersCount", minListenersCount.toString())
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

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(pageResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_artists_shouldReturnError_whenServiceFails() throws Exception {
        // Given
        String errorMessage = "Failed to fetch artists: service error occurred";
        
        when(artistService.findAll(any(ArtistSearchParams.class), any(Pageable.class)))
            .thenThrow(new RuntimeException("Test exception"));

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.failureBody(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void PATCH_artistApproval_shouldUpdateApprovalStatus_whenValidStatusProvided() throws Exception {
        ApprovalStatusRequestDto request = new ApprovalStatusRequestDto(2);
        LastfmArtistResponseDto responseDto = LastfmArtistResponseDto.from(mockArtist);
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(responseDto));

        when(artistService.updateApprovalStatus(mockArtist.getId(), request.approvalStatus()))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/artists/{id}/approval", mockArtist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void PATCH_artistApproval_shouldReturnError_whenServiceFails() throws Exception {
        ApprovalStatusRequestDto request = new ApprovalStatusRequestDto(2);
        String errorMessage = "Failed to update approval status: service error occurred";
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.failureBody(errorMessage));

        when(artistService.updateApprovalStatus(mockArtist.getId(), request.approvalStatus()))
            .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(patch("/api/v1/artists/{id}/approval", mockArtist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));
    }
}