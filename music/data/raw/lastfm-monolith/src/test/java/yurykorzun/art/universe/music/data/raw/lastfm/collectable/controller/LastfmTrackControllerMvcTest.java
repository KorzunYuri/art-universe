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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller.LastfmTrackController;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseMvcTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LastfmTrackController.class)
class LastfmTrackControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmTrackService trackService;

    private List<LastfmTrack> mockTracks;
    private LastfmTrack mockTrack;
    private LastfmArtist mockArtist;
    private Pageable defaultPageable = PageRequest.of(0, 20, Sort.by(Sort.Order.asc("name")));

    @BeforeEach
    void setUp() {
        mockArtist = EntityCreationHelper.createArtist(builder -> 
            builder.id(1L).name("Test Artist"));
            
        mockTrack = LastfmTrack.builder()
            .id(1L)
            .name("Test Track")
            .mbid("mbid-123")
            .url("http://test.com/track")
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(1000L)
            .listenersCount(500)
            .artist(mockArtist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        LastfmTrack anotherMockedTrack = LastfmTrack.builder()
            .id(2L)
            .name("Another Track")
            .mbid("mbid-456")
            .url("http://another.com/track")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(2000L)
            .listenersCount(1000)
            .artist(mockArtist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        mockTracks = Arrays.asList(
            mockTrack,
            anotherMockedTrack
        );
    }

    @Test
    void GET_trackById_shouldReturnTrack_whenFound() throws Exception {
        // Given
        Long trackId = 1L;
        LastfmTrackResponseDto responseDto = LastfmTrackResponseDto.from(mockTrack);
        
        when(trackService.findDtoById(eq(trackId)))
            .thenReturn(responseDto);

        String expectedJson = objectMapper.writeValueAsString(responseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/tracks/{id}", trackId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getTracks_shouldReturnTracksPage() throws Exception {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 1L;
        
        Page<LastfmTrack> tracksPage = new PageImpl<>(mockTracks, defaultPageable, mockTracks.size());
        Page<LastfmTrackResponseDto> pageResponse = tracksPage.map(LastfmTrackResponseDto::from);
        
        when(trackService.findAll(any(TrackSearchParams.class), any(Pageable.class)))
            .thenReturn(pageResponse);

        String expectedJson = objectMapper.writeValueAsString(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tracks")
                .param("search", search)
                .param("minPlayCount", minPlayCount.toString())
                .param("minListenersCount", minListenersCount.toString())
                .param("artistId", artistId.toString())
                .param("tagId", "123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getTracks_shouldHandleNullFilters() throws Exception {
        // Given
        Page<LastfmTrack> tracksPage = new PageImpl<>(mockTracks, defaultPageable, mockTracks.size());
        Page<LastfmTrackResponseDto> pageResponse = tracksPage.map(LastfmTrackResponseDto::from);
        
        when(trackService.findAll(any(TrackSearchParams.class), any(Pageable.class)))
            .thenReturn(pageResponse);

        String expectedJson = objectMapper.writeValueAsString(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tracks")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void updateApprovalStatus_shouldUpdateApprovalStatus_whenValidStatusProvided() throws Exception {
        ApprovalStatusRequestDto request = new ApprovalStatusRequestDto(2);
        LastfmTrackResponseDto responseDto = LastfmTrackResponseDto.from(mockTrack);
        String expectedJson = objectMapper.writeValueAsString(responseDto);

        when(trackService.updateApprovalStatus(mockTrack.getId(), request.approvalStatus()))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/tracks/{id}/approval", mockTrack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
}
