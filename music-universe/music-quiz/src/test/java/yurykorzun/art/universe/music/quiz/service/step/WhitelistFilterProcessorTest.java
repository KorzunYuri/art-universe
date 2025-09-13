package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import java.util.List;
import java.util.Map;

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
    void validate_shouldPass_whenValidParams() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.WHITELIST_FILTER);
        step.setParams(Map.of("categories", List.of(
            Map.of("id", 1L, "weight", 0.5)
        )));

        // when & then
        assertDoesNotThrow(() -> processor.validate(step));
    }

    @Test
    void validate_shouldThrow_whenMissingCategories() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.WHITELIST_FILTER);
        step.setParams(Map.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.validate(step)
        );
        assertEquals("Whitelist step requires 'categories' parameter", exception.getMessage());
    }

    @Test
    void validate_shouldThrow_whenCategoryMissingWeight() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.WHITELIST_FILTER);
        step.setParams(Map.of("categories", List.of(
            Map.of("id", 1L) // missing weight
        )));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.validate(step)
        );
        assertEquals("Each category must have 'id' and 'weight' fields", exception.getMessage());
    }

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.WHITELIST_FILTER);
        step.setParams(Map.of("categories", List.of(
            Map.of("id", 1L, "weight", 0.5),
            Map.of("id", 2L, "weight", 0.7)
        )));

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
}
