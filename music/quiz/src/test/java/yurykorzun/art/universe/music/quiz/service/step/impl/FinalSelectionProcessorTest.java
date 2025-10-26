package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.CommonTestConfig;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalSelectionProcessorTest {

    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private FinalSelectionProcessor processor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = CommonTestConfig.getObjectMapper();
        processor = new FinalSelectionProcessor(stepRunRepository, stepRepository, objectMapper);
        ReflectionTestUtils.setField(processor, "entityManager", entityManager);
    }

    @Test
    void processStep_shouldCallProcedure_whenValidInput() {
        // given
        Step step = Step.builder()
            .id(1L)
            .type(StepType.FINAL_SELECTION)
            .cfgData("{\"targetCount\":20}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // when
        StepRunResult result = ReflectionTestUtils.invokeMethod(processor, "processStep", 
            step, "input.table", "output.table", stepRun);

        // then
        assertNotNull(result);
        assertEquals("output.table", result.getOutputTableName());
        verify(entityManager).createNativeQuery(contains("p_quiz_gen_tracks_step_final_selection"));
        verify(query).setParameter("inputTable", "input.table");
        verify(query).setParameter("outputTable", "output.table");
        verify(query).setParameter("targetCount", 20);
        verify(query).executeUpdate();
    }

    @Test
    void validateConfiguration_shouldPass_whenValidConfig() {
        // given
        String validConfig = "{\"targetCount\":20}";

        // when & then
        assertDoesNotThrow(() -> processor.validateConfiguration(validConfig));
    }

    @Test
    void validateConfiguration_shouldThrow_whenInvalidJson() {
        // given
        String invalidConfig = "invalid json";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> processor.validateConfiguration(invalidConfig));
        
        assertEquals("Invalid configuration for final selection step", exception.getMessage());
    }
}
