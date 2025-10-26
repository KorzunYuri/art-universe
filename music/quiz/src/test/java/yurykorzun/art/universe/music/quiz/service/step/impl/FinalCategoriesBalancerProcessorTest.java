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
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.CommonTestConfig;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.FinalCategoriesBalancerStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalCategoriesBalancerProcessorTest {

    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private FinalCategoriesBalancerProcessor processor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = CommonTestConfig.getObjectMapper();
        processor = new FinalCategoriesBalancerProcessor(stepRunRepository, stepRepository, objectMapper);
        ReflectionTestUtils.setField(processor, "entityManager", entityManager);
    }

    @Test
    void processStep_shouldCallProcedure_whenValidInput() {
        // given
        Step step = Step.builder()
            .id(1L)
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0},{\"id\":2,\"weight\":0.5}]}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();
        
        // Mock getStepMetadata for generateAuxiliaryTableName
        var metadata = mock(yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection.class);
        when(metadata.getGameId()).thenReturn(1L);
        when(metadata.getPipelineId()).thenReturn(1L);
        when(stepRepository.getStepMetadata(1L)).thenReturn(metadata);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // when
        StepRunResult result = ReflectionTestUtils.invokeMethod(processor, "processStep", 
            step, "input.table", "output.table", stepRun);

        // then
        assertNotNull(result);
        assertEquals("output.table", result.getOutputTableName());
        verify(entityManager, times(4)).createNativeQuery(anyString()); // DROP, CREATE, INSERT, PROCEDURE
        verify(query, atLeast(1)).executeUpdate();
    }

    @Test
    void validateConfiguration_shouldPass_whenValidConfig() {
        // given
        String validConfig = "{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0}]}";

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
        
        assertEquals("Invalid configuration for categories balancer step", exception.getMessage());
    }

    @Test
    void getResultStats_shouldCalculateOutputAndDefaultQuota_whenValidData() {
        // given
        final String inputTableName = "input.table";
        final String resultTableName = "output.table";
        StepRun stepRun = StepRun.builder()
            .inputTableName(inputTableName)
            .resultTableName(resultTableName)
            .stepCfgData("{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0},{\"id\":2,\"weight\":0.5}]}")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, inputTableName)).thenReturn(true);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, resultTableName)).thenReturn(true);
            
            when(entityManager.createNativeQuery(anyString())).thenReturn(query);
            when(query.setParameter(anyString(), any())).thenReturn(query);
            when(query.getSingleResult()).thenReturn(10L);
            when(query.getResultList()).thenReturn(List.of(new Object[]{5L}));

            // when
            FinalCategoriesBalancerStats result = (FinalCategoriesBalancerStats) processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            assertNotNull(result.getOutputRecordsByCategory());
            assertNotNull(result.getOutputArtistsByCategory());
            assertTrue(result.getOutputRecordsByCategory().isEmpty());
            assertTrue(result.getOutputArtistsByCategory().isEmpty());
            assertEquals(0L, result.getDefaultQuotaRecords());
            assertEquals(0L, result.getDefaultQuotaArtists());
        }
    }

    @Test
    void getResultStats_shouldReturnEmptyStats_whenInputTableNotExists() {
        // given
        final String inputTableName = "input.table";
        final String resultTableName = "output.table";
        StepRun stepRun = StepRun.builder()
            .inputTableName(inputTableName)
            .resultTableName(resultTableName)
            .stepCfgData("{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0}]}")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, inputTableName)).thenReturn(false);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, resultTableName)).thenReturn(true);
            
            when(entityManager.createNativeQuery(anyString())).thenReturn(query);
            when(query.getSingleResult()).thenReturn(0L);
            
            // when
            FinalCategoriesBalancerStats result = (FinalCategoriesBalancerStats) processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            assertTrue(result.getOutputRecordsByCategory().isEmpty());
            assertTrue(result.getOutputArtistsByCategory().isEmpty());
            assertEquals(0L, result.getDefaultQuotaRecords());
            assertEquals(0L, result.getDefaultQuotaArtists());
        }
    }

    @Test
    void getResultStats_shouldReturnEmptyStats_whenOutputTableNotExists() {
        // given
        final String inputTableName = "input.table";
        final String resultTableName = "output.table";
        StepRun stepRun = StepRun.builder()
            .inputTableName(inputTableName)
            .resultTableName(resultTableName)
            .stepCfgData("{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0}]}")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, inputTableName)).thenReturn(true);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, resultTableName)).thenReturn(false);
            
            when(entityManager.createNativeQuery(anyString())).thenReturn(query);
            when(query.getSingleResult()).thenReturn(0L);
            
            // when
            FinalCategoriesBalancerStats result = (FinalCategoriesBalancerStats) processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            assertTrue(result.getOutputRecordsByCategory().isEmpty());
            assertTrue(result.getOutputArtistsByCategory().isEmpty());
            assertEquals(0L, result.getDefaultQuotaRecords());
            assertEquals(0L, result.getDefaultQuotaArtists());
        }
    }
}
