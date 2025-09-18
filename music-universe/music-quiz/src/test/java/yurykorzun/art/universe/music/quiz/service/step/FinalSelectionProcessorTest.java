package yurykorzun.art.universe.music.quiz.service.step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.step.FinalSelectionStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalSelectionProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private FinalSelectionProcessor processor;

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        FinalSelectionStep step = new FinalSelectionStep(25);

        when(pipelineRepository.finalSelection(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).finalSelection("schema", "table", 1L, 2L, 3, 25);
    }

    @Test
    void process_shouldThrowException_whenInputTableInvalid() {
        // given
        FinalSelectionStep step = new FinalSelectionStep(20);

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
        FinalSelectionStep step = new FinalSelectionStep(15);
        RuntimeException repositoryException = new RuntimeException("Repository error");

        when(pipelineRepository.finalSelection(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt()))
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
    void step_shouldBeFinal() {
        // given
        FinalSelectionStep step = new FinalSelectionStep(20);

        // when & then
        assertTrue(step.isFinal());
    }

    @Test
    void step_shouldReturnTargetCount() {
        // given
        FinalSelectionStep step = new FinalSelectionStep(25);

        // when & then
        assertEquals(25, step.getTargetCount());
    }
}
