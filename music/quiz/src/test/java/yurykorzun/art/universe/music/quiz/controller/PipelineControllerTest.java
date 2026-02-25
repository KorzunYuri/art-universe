package yurykorzun.art.universe.music.quiz.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineControllerTest {

    @Mock
    private PipelineService pipelineService;

    @InjectMocks
    private PipelineController pipelineController;

    @Test
    void getPipeline_shouldReturnPipelineDto_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of())
            .build();

        when(pipelineService.getPipeline(pipelineId)).thenReturn(expectedDto);

        // when
        PipelineDto result = pipelineController.getPipeline(pipelineId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(pipelineService).getPipeline(pipelineId);
    }

    @Test
    void addStep_shouldReturnUpdatedPipeline_whenSuccessful() {
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

        // when
        PipelineDto result = pipelineController.addStep(pipelineId, stepDto, position);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(pipelineService).addStep(pipelineId, stepDto, position);
    }

    @Test
    void executeStep_shouldReturnUpdatedPipeline_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L;
        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of())
            .build();

        when(pipelineService.getPipeline(pipelineId)).thenReturn(expectedDto);

        // when
        PipelineDto result = pipelineController.executeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(pipelineService).executeStep(pipelineId, stepId);
        verify(pipelineService).getPipeline(pipelineId);
    }

    @Test
    void removeStep_shouldReturnUpdatedPipeline_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L;
        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of())
            .build();

        when(pipelineService.removeStep(pipelineId, stepId)).thenReturn(expectedDto);

        // when
        PipelineDto result = pipelineController.removeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(pipelineService).removeStep(pipelineId, stepId);
    }

    @Test
    void updateStepConfiguration_shouldReturnUpdatedPipeline_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L;
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":30}")
            .build();

        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of(stepDto))
            .build();

        when(pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto)).thenReturn(expectedDto);

        // when
        PipelineDto result = pipelineController.updateStepConfiguration(pipelineId, stepId, stepDto);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(pipelineService).updateStepConfiguration(pipelineId, stepId, stepDto);
    }

    @Test
    void getStepPreview_shouldReturnPreview_whenSuccessful() {
        // given
        Long stepId = 1L;
        String expectedPreview = "{\"preview\": \"data\"}";

        when(pipelineService.getStepPreview(stepId)).thenReturn(expectedPreview);

        // when
        String result = pipelineController.getStepPreview(stepId);

        // then
        assertEquals(expectedPreview, result);
        verify(pipelineService).getStepPreview(stepId);
    }

    @Test
    void moveStep_shouldReturnUpdatedPipeline_whenSuccessful() {
        // given
        Long pipelineId = 4L;
        Long stepId = 2L;
        PipelineDto expectedDto = PipelineDto.builder()
            .id(pipelineId)
            .immutable(false)
            .steps(List.of())
            .build();

        when(pipelineService.moveStep(pipelineId, stepId, 0)).thenReturn(expectedDto);

        // when
        PipelineDto result = pipelineController.moveStep(pipelineId, stepId, 0);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(pipelineService).moveStep(pipelineId, stepId, 0);
    }
}
