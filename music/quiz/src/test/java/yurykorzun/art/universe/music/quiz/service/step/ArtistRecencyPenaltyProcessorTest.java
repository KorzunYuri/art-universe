package yurykorzun.art.universe.music.quiz.service.step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.step.ArtistRecencyPenaltyStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistRecencyPenaltyProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private ArtistRecencyPenaltyProcessor processor;

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        ArtistRecencyPenaltyStep step = new ArtistRecencyPenaltyStep();

        when(pipelineRepository.artistRecencyPenalty(anyString(), anyString(), anyLong(), anyLong(), anyInt()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).artistRecencyPenalty("schema", "table", 1L, 2L, 3);
    }

    @Test
    void process_shouldThrowException_whenInputTableInvalid() {
        // given
        ArtistRecencyPenaltyStep step = new ArtistRecencyPenaltyStep();

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
        ArtistRecencyPenaltyStep step = new ArtistRecencyPenaltyStep();
        RuntimeException repositoryException = new RuntimeException("Repository error");

        when(pipelineRepository.artistRecencyPenalty(anyString(), anyString(), anyLong(), anyLong(), anyInt()))
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
        ArtistRecencyPenaltyStep step = new ArtistRecencyPenaltyStep();

        // when & then
        assertFalse(step.isFinal());
    }
}
