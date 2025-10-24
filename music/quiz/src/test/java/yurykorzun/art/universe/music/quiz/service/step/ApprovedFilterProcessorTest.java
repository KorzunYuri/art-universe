package yurykorzun.art.universe.music.quiz.service.step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepPosition;
import yurykorzun.art.universe.music.quiz.entity.step.middle.ApprovedFilterStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovedFilterProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private ApprovedFilterProcessor processor;

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        ApprovedFilterStep step = new ApprovedFilterStep();

        when(pipelineRepository.approvedFilter(anyString(), anyString(), anyLong(), anyLong(), anyInt()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).approvedFilter("schema", "table", 1L, 2L, 3);
    }

    @Test
    void process_shouldThrowException_whenInputTableInvalid() {
        // given
        ApprovedFilterStep step = new ApprovedFilterStep();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.process("invalid_table", 1L, 2L, 3, step)
        );
        
        assertEquals("Input table must be in format 'schema.table'", exception.getMessage());
        verifyNoInteractions(pipelineRepository);
    }

    @Test
    void process_shouldPropagateException_whenRepositoryThrows() {
        // given
        ApprovedFilterStep step = new ApprovedFilterStep();
        RuntimeException repositoryException = new RuntimeException("Repository error");

        when(pipelineRepository.approvedFilter(anyString(), anyString(), anyLong(), anyLong(), anyInt()))
            .thenThrow(repositoryException);

        // when & then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> processor.process("schema.table", 1L, 2L, 3, step)
        );
        
        assertEquals("Repository error", exception.getMessage());
        assertSame(repositoryException, exception);
    }

    @Test
    void step_shouldNotBeFinal() {
        // given
        ApprovedFilterStep step = new ApprovedFilterStep();

        // when & then
        assertSame(GenerationStepPosition.MIDDLE, step.getPosition());
    }
}
