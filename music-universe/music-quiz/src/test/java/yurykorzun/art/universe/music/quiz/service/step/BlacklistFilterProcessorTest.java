package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.step.BlacklistFilterStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistFilterProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private BlacklistFilterProcessor processor;

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        BlacklistFilterStep step = new BlacklistFilterStep(List.of(1L, 2L));

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(pipelineRepository.blacklistFilter(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyString(), anyString()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).blacklistFilter("schema", "table", 1L, 2L, 3, "mu_quiz_stg", "game_config_blacklist_1");
        verify(entityManager, times(4)).createNativeQuery(anyString());
        verify(query, times(2)).setParameter(anyInt(), any());
        verify(query, times(4)).executeUpdate();
    }

    @Test
    void process_shouldThrowException_whenInputTableInvalid() {
        // given
        BlacklistFilterStep step = new BlacklistFilterStep(List.of(1L, 2L));

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
        BlacklistFilterStep step = new BlacklistFilterStep(List.of(1L, 2L));
        RuntimeException repositoryException = new RuntimeException("Repository error");

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(pipelineRepository.blacklistFilter(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyString(), anyString()))
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
        BlacklistFilterStep step = new BlacklistFilterStep(List.of(1L, 2L));

        // when & then
        assertFalse(step.isFinal());
    }
}
