package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.PipelineRun;
import yurykorzun.art.universe.music.quiz.repository.PipelineRunRepository;
import yurykorzun.art.universe.music.quiz.service.impl.PipelineRunServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineRunServiceTest {

    @Mock
    private PipelineRunRepository pipelineRunRepository;

    @InjectMocks
    private PipelineRunServiceImpl pipelineRunService;

    @Test
    void createPipelineRun_shouldCreateAndSavePipelineRun() {
        // given
        PipelineRun savedPipelineRun = PipelineRun.builder()
            .id(1L)
            .pipelineId(1L)
            .build();

        when(pipelineRunRepository.save(any(PipelineRun.class))).thenReturn(savedPipelineRun);

        // when
        PipelineRun result = pipelineRunService.createPipelineRun(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPipelineId());
        verify(pipelineRunRepository).save(any(PipelineRun.class));
    }

    @Test
    void startPipelineRun_shouldUpdateStatusAndStartTime() {
        // given
        PipelineRun pipelineRun = PipelineRun.builder()
            .id(1L)
            .status(ExecutionStatus.PENDING)
            .build();

        when(pipelineRunRepository.findById(1L)).thenReturn(Optional.of(pipelineRun));

        // when
        pipelineRunService.startPipelineRun(1L);

        // then
        assertEquals(ExecutionStatus.STARTED, pipelineRun.getStatus());
        assertNotNull(pipelineRun.getStartedAt());
        verify(pipelineRunRepository).save(pipelineRun);
    }

    @Test
    void startPipelineRun_shouldThrowException_whenPipelineRunNotFound() {
        // given
        when(pipelineRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineRunService.startPipelineRun(999L)
        );
        
        assertEquals("Pipeline run not found: 999", exception.getMessage());
    }

    @Test
    void completePipelineRun_shouldUpdateStatusAndResult() {
        // given
        PipelineRun pipelineRun = PipelineRun.builder()
            .id(1L)
            .status(ExecutionStatus.STARTED)
            .build();

        when(pipelineRunRepository.findById(1L)).thenReturn(Optional.of(pipelineRun));

        // when
        pipelineRunService.completePipelineRun(1L, "result.table");

        // then
        assertEquals(ExecutionStatus.COMPLETED, pipelineRun.getStatus());
        assertEquals("result.table", pipelineRun.getResultTableName());
        assertNotNull(pipelineRun.getCompletedAt());
        verify(pipelineRunRepository).save(pipelineRun);
    }

    @Test
    void completePipelineRun_shouldThrowException_whenPipelineRunNotFound() {
        // given
        when(pipelineRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineRunService.completePipelineRun(999L, "result.table")
        );
        
        assertEquals("Pipeline run not found: 999", exception.getMessage());
    }

    @Test
    void failPipelineRun_shouldMarkPipelineRunAsFailed() {
        // given
        PipelineRun pipelineRun = PipelineRun.builder()
            .id(1L)
            .status(ExecutionStatus.STARTED)
            .build();

        when(pipelineRunRepository.findById(1L)).thenReturn(Optional.of(pipelineRun));

        // when
        pipelineRunService.failPipelineRun(1L);

        // then
        assertEquals(ExecutionStatus.FAILED, pipelineRun.getStatus());
        assertNotNull(pipelineRun.getCompletedAt());
        verify(pipelineRunRepository).save(pipelineRun);
    }

    @Test
    void failPipelineRun_shouldThrowException_whenPipelineRunNotFound() {
        // given
        when(pipelineRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineRunService.failPipelineRun(999L)
        );
        
        assertEquals("Pipeline run not found: 999", exception.getMessage());
    }
}
