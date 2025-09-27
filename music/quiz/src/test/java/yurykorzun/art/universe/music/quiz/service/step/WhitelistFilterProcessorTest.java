package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.step.WhitelistFilterStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhitelistFilterProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private WhitelistFilterProcessor processor;

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        WhitelistFilterStep step = new WhitelistFilterStep(List.of(
            new WhitelistFilterStep.CategoryWeight(1L, 0.5),
            new WhitelistFilterStep.CategoryWeight(2L, 0.7)
        ));

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(pipelineRepository.whitelistFilter(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyString(), anyString()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).whitelistFilter("schema", "table", 1L, 2L, 3, "mu_quiz_stg", "game_config_whitelist_1");
        verify(entityManager, times(3)).createNativeQuery(anyString());
        verify(query, times(4)).setParameter(anyInt(), any());
        verify(query, times(4)).executeUpdate();
    }

    @Test
    void process_shouldThrowException_whenInputTableInvalid() {
        // given
        WhitelistFilterStep step = new WhitelistFilterStep(List.of(
            new WhitelistFilterStep.CategoryWeight(1L, 0.5)
        ));

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
        WhitelistFilterStep step = new WhitelistFilterStep(List.of(
            new WhitelistFilterStep.CategoryWeight(1L, 0.5)
        ));
        RuntimeException repositoryException = new RuntimeException("Repository error");

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(pipelineRepository.whitelistFilter(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyString(), anyString()))
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
        WhitelistFilterStep step = new WhitelistFilterStep(List.of());

        // when & then
        assertFalse(step.isFinal());
    }
}
