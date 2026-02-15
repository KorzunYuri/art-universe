package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ArtistSearchRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LastfmArtistController.class)
class LastfmArtistControllerMvcTest extends BaseMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmArtistService artistService;

    @MockitoBean
    private LastfmArtistSearchRequestService artistSearchRequestService;

    private List<LastfmArtist> mockArtists;
    private LastfmArtist mockArtist;

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

}
