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
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void PATCH_updateApprovalStatus_shouldUpdateApprovalStatus_whenValidStatusProvided() throws Exception {
        ApprovalStatusRequestDto request = new ApprovalStatusRequestDto(2);
        LastfmTagResponseDto responseDto = LastfmTagResponseDto.from(mockTag);
        String expectedJson = objectMapper.writeValueAsString(responseDto);

        when(tagService.updateApprovalStatus(mockTag.getId(), request.approvalStatus()))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/tags/{id}/approval", mockTag.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

}
