package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.CommonTestConfig;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.FinalCategoriesBalancerStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalCategoriesBalancerProcessorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private FinalCategoriesBalancerProcessor processor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = CommonTestConfig.getObjectMapper();
        processor = new FinalCategoriesBalancerProcessor(mock(StepProcessorRegistry.class), objectMapper);
        processor.setEntityManager(entityManager);
    }

    @Test
    void processStep_shouldThrowException_whenNullInputTable() {
        // given
        Step step = Step.builder().cfgData("{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0}]}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, null, "output.table", stepRun)
        );

        assertEquals("DB object name cannot be null or empty", exception.getMessage());
    }

    @Test
    void processStep_shouldThrowException_whenInvalidInputTableFormat() {
        // given
        Step step = Step.builder().cfgData("{\"targetCount\":20,\"defaultQuota\":0.5,\"categories\":[{\"id\":1,\"weight\":1.0}]}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, "invalid_table", "output.table", stepRun)
        );

        assertEquals("DB object name must be in format 'schemaName.table', got: 'invalid_table'", exception.getMessage());
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
        
        final String inputTableName = "input.table";
        final String stepTableNameBase = "output.table";
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(query.getSingleResult()).thenReturn(null);

        // when
        StepRunResult result = processor.processStep(step, inputTableName, stepTableNameBase, stepRun);

        // then
        assertNotNull(result);
        assertEquals(stepTableNameBase + "_categories_balancer", result.getOutputTableName());
        verify(entityManager, times(4)).createNativeQuery(anyString()); // DROP, CREATE, INSERT, PROCEDURE
        verify(query, atLeast(1)).executeUpdate();
        verify(query).getSingleResult();
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
            
            // Mock basic stats calls
            Query inputCountQuery = mock(Query.class);
            Query inputArtistQuery = mock(Query.class);
            Query outputCountQuery = mock(Query.class);
            Query outputArtistQuery = mock(Query.class);
            
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM " + inputTableName)).thenReturn(inputCountQuery);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + inputTableName)).thenReturn(inputArtistQuery);
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM " + resultTableName)).thenReturn(outputCountQuery);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + resultTableName)).thenReturn(outputArtistQuery);
            
            when(inputCountQuery.getSingleResult()).thenReturn(10L);
            when(inputArtistQuery.getSingleResult()).thenReturn(5L);
            when(outputCountQuery.getSingleResult()).thenReturn(8L);
            when(outputArtistQuery.getSingleResult()).thenReturn(4L);
            
            // Mock category-specific and default quota queries
            Query categoryQuery = mock(Query.class);
            when(entityManager.createNativeQuery(contains("ac.category_id = :categoryId"))).thenReturn(categoryQuery);
            when(categoryQuery.setParameter(anyString(), any())).thenReturn(categoryQuery);
            when(categoryQuery.getResultList()).thenReturn(List.of(new Object[]{5L}));

            // when
            FinalCategoriesBalancerStats result = (FinalCategoriesBalancerStats) processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            // Verify basic stats are copied
            assertEquals(10L, result.getInputRecords());
            assertEquals(5L, result.getInputArtists());
            assertEquals(2L, result.getFilteredRecords());
            assertEquals(1L, result.getFilteredArtists());
            assertEquals(8L, result.getOutputRecords());
            assertEquals(4L, result.getOutputArtists());
            // Verify extended stats
            assertNotNull(result.getOutputRecordsByCategory());
            assertNotNull(result.getOutputArtistsByCategory());
            assertNotNull(result.getDefaultQuotaRecords());
            assertNotNull(result.getDefaultQuotaArtists());
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
            
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM " + resultTableName)).thenReturn(query);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + resultTableName)).thenReturn(query);
            when(query.getSingleResult()).thenReturn(8L).thenReturn(4L);

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
            
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM " + inputTableName)).thenReturn(query);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + inputTableName)).thenReturn(query);
            when(query.getSingleResult()).thenReturn(10L).thenReturn(5L);

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
