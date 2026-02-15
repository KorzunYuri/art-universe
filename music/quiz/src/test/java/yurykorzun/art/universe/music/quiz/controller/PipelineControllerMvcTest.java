package yurykorzun.art.universe.music.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PipelineController.class)
class PipelineControllerMvcTest extends BaseMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PipelineService pipelineService;

    @Test
    void GET_pipeline_shouldReturnPipelineDto_whenSuccessful() throws Exception {
        // given
        Long pipelineId = 1L;
        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of())
            .build();

        when(pipelineService.getPipeline(pipelineId)).thenReturn(expectedDto);

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(get("/api/v1/pipeline/{pipelineId}", pipelineId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(pipelineService).getPipeline(pipelineId);
    }

    @Test
    void POST_pipelineSteps_shouldReturnUpdatedPipeline_whenSuccessful() throws Exception {
        // given
        Long pipelineId = 1L;
        Integer position = 1;
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of(stepDto))
            .build();

        when(pipelineService.addStep(pipelineId, stepDto, position)).thenReturn(expectedDto);

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(post("/api/v1/pipeline/{pipelineId}/steps", pipelineId)
                .param("position", position.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stepDto)))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(pipelineService).addStep(pipelineId, stepDto, position);
    }
}
