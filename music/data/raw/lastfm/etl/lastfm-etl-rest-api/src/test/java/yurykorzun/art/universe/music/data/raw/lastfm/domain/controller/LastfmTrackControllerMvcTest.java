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
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LastfmTrackController.class)
class LastfmTrackControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmTrackService trackService;

    private LastfmTrack mockTrack;
    private LastfmArtist mockArtist;

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
    }

    @Test
    void PATCH_updateApprovalStatus_shouldUpdateApprovalStatus_whenValidStatusProvided() throws Exception {
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
