package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.impl.PipelineServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private PipelineRepository pipelineRepository;
    
    @Mock
    private StepRepository stepRepository;
    
    @Mock
    private PipelineStepRepository pipelineStepRepository;
    
    @Mock
    private PipelineRunRepository pipelineRunRepository;
    
    @Mock
    private StepRunRepository stepRunRepository;

    @InjectMocks
    private PipelineServiceImpl pipelineService;

    @Test
    void createBasicPipeline_shouldReturnPipelineDto_whenSuccessful() {
        // given
        Pipeline savedPipeline = Pipeline.builder()
            .id(1L)
            .immutable(false)
            .build();

        when(pipelineRepository.save(any(Pipeline.class))).thenReturn(savedPipeline);

        // when
        PipelineDto result = pipelineService.createBasicPipeline();

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertFalse(result.getImmutable());
        assertTrue(result.getSteps().isEmpty());
        
        verify(pipelineRepository).save(any(Pipeline.class));
    }

    @Test
    void getPipeline_shouldReturnPipelineDto_whenPipelineExists() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder()
            .id(pipelineId)
            .immutable(false)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.getPipeline(pipelineId);

        // then
        assertNotNull(result);
        assertEquals(pipelineId, result.getId());
        assertTrue(result.getSteps().isEmpty());
        
        verify(pipelineRepository).findById(pipelineId);
        verify(pipelineStepRepository).findPipelineStepsWithDetails(pipelineId);
    }

    @Test
    void getPipeline_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.getPipeline(pipelineId)
        );
        
        assertEquals("Pipeline not found: 999", exception.getMessage());
        verify(pipelineRepository).findById(pipelineId);
    }

    @Test
    void validatePipelineForGeneration_shouldThrowException_whenNoSteps() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder()
            .id(pipelineId)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );
        
        assertEquals("Pipeline must have at least one step", exception.getMessage());
    }
}
