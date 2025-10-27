package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistRecencyPenaltyProcessorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private ArtistRecencyPenaltyProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ArtistRecencyPenaltyProcessor(mock(StepProcessorRegistry.class));
        processor.setEntityManager(entityManager);
    }

    @Test
    void processStep_shouldCallProcedure_whenValidInput() {
        // given
        Step step = Step.builder()
            .id(1L)
            .type(StepType.ARTIST_RECENCY_PENALTY)
            .cfgData("{}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();
        
        final String inputTableName = "input.table";
        final String stepTableNameBase = "output.table";
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);

        // when
        StepRunResult result = processor.processStep(step, inputTableName, stepTableNameBase, stepRun);

        // then
        assertNotNull(result);
        assertEquals(stepTableNameBase + "_artist_recency", result.getOutputTableName());
        verify(entityManager).createNativeQuery(contains("p_quiz_gen_tracks_step_artist_recency_penalty"));
        verify(query).setParameter("inputTable", inputTableName);
        verify(query).setParameter("outputTable", stepTableNameBase + "_artist_recency");
        verify(query).getSingleResult();
    }

    @Test
    void getPreview_shouldReturnEmptyJson() {
        // given
        Step step = Step.builder().build();

        // when
        String result = processor.getPreview(step);

        // then
        assertEquals("{}", result);
    }

    @Test
    void verifyConfigurationIsActual_shouldReturnSameConfig_whenValid() {
        // given
        String validConfig = "{}";

        // when
        String result = processor.verifyConfigurationIsActual(validConfig);

        // then
        assertEquals(validConfig, result);
    }

    @Test
    void validateConfiguration_shouldPass_whenValidConfig() {
        // given
        String validConfig = "{}";

        // when & then
        assertDoesNotThrow(() -> processor.validateConfiguration(validConfig));
    }

    @Test
    void isActualVersion_shouldReturnTrue() {
        // when
        boolean result = processor.isActualVersion("{}");

        // then
        assertTrue(result);
    }

    @Test
    void migrateConfiguration_shouldReturnSameConfig() {
        // given
        String config = "{}";

        // when
        String result = processor.migrateConfiguration(config);

        // then
        assertEquals(config, result);
    }
}
