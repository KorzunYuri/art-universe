package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.BasicStepStats;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class BaseGenerationStepProcessor implements GenerationStepProcessor {
    
    private final StepType stepType;
    private final StepRunRepository stepRunRepository;
    private final StepRepository stepRepository;
    
    @PersistenceContext
    protected EntityManager entityManager;
    
    protected final ObjectMapper objectMapper;
    
    // Cache for output table names with simple LRU behavior
    private static final ConcurrentMap<Long, String> outputTableCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;
    
    protected BaseGenerationStepProcessor(StepType stepType, StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        this.stepType = stepType;
        this.stepRunRepository = stepRunRepository;
        this.stepRepository = stepRepository;
        this.objectMapper = objectMapper;
        GenerationStepProcessorRegistry.register(this);
    }
    
    @Override
    public StepType getStepType() {
        return stepType;
    }
    
    @Override
    public Integer getStepTypeVersion() {
        return stepType.getVersion();
    }
    
    /**
     * Processes a step and returns the completed StepRun with statistics.
     */
    @Override
    public StepRun process(Step step, String inputTableName, @Nullable Long pipelineRunId) {
        validateStep(step);
        if (inputTableName != null) {
            validateInputTable(inputTableName);
        }
        
        // Create StepRun
        StepRun stepRun = StepRun.builder()
            .pipelineRunId(pipelineRunId)
            .stepId(step.getId())
            .stepType(step.getType())
            .algVersion(step.getAlgVersion())
            .stepCfgData(step.getCfgData())
            .inputTableName(inputTableName)
            .status(ExecutionStatus.PENDING)
            .build();
        
        StepRun savedStepRun = stepRunRepository.save(stepRun);
        
        // Generate output table name and cache it
        String outputTableName = generateOutputTableName(step, savedStepRun, pipelineRunId);
        cacheOutputTableName(savedStepRun.getId(), outputTableName);
        
        // Update status to STARTED
        savedStepRun.setStatus(ExecutionStatus.STARTED);
        savedStepRun.setStartedAt(Instant.now());
        savedStepRun.setResultTableName(outputTableName);
        stepRunRepository.save(savedStepRun);
        
        try {
            // Measure execution time
            long startTime = System.currentTimeMillis();
            
            // Call actual step processing procedure
            StepRunResult result = processStep(step, inputTableName, outputTableName, savedStepRun);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Update output table name from result
            savedStepRun.setResultTableName(result.getOutputTableName());
            
            // Calculate statistics
            StepRunStats stats = getResultStats(savedStepRun);
            stats.setExecutionTimeMs(executionTime);
            
            // Serialize stats to JSON
            String statsJson = objectMapper.writeValueAsString(stats);
            savedStepRun.setResultStats(statsJson);
            
            // Mark as completed
            savedStepRun.setStatus(ExecutionStatus.COMPLETED);
            savedStepRun.setCompletedAt(Instant.now());
            stepRunRepository.save(savedStepRun);
            
            // Update step's lastStepRunId
            step.setLastStepRunId(savedStepRun.getId());
            stepRepository.save(step);
            
            return savedStepRun;
            
        } catch (Exception e) {
            savedStepRun.setStatus(ExecutionStatus.FAILED);
            savedStepRun.setCompletedAt(Instant.now());
            stepRunRepository.save(savedStepRun);
            throw new RuntimeException("Step processing failed", e);
        }
    }
    
    @Override
    public void validateConfiguration(String cfgData) {
        // override for processors that have configurations
    }

    @Override
    public String migrateConfiguration(String cfgData, Integer fromVersion, Integer toVersion) {
        // backward compatibility is assumed by default
        return cfgData;
    }
    
    @Override
    public String getPreview(String cfgData) {
        return "{}";
    }

    @Override
    public StepRunStats getResultStats(StepRun stepRun) {
        BasicStepStats stats = new BasicStepStats();
        
        String inputTableName = stepRun.getInputTableName();
        String outputTableName = stepRun.getResultTableName();
        
        // Check table existence
        boolean inputExists = inputTableName != null && DatabaseUtils.tableExists(entityManager, inputTableName);
        boolean outputExists = outputTableName != null && DatabaseUtils.tableExists(entityManager, outputTableName);
        
        if (!inputExists && !outputExists) {
            // Both tables don't exist - set all to 0
            stats.setInputRecords(0L);
            stats.setInputArtists(0L);
            stats.setFilteredRecords(0L);
            stats.setFilteredArtists(0L);
            stats.setOutputRecords(0L);
            stats.setOutputArtists(0L);
        } else if (!inputExists) {
            // Input doesn't exist (START_DATASOURCE case) - copy output stats to input stats
            Long outputRecords = outputExists ? getRecordCount(outputTableName) : 0L;
            Long outputArtists = outputExists ? getArtistCount(outputTableName) : 0L;
            
            stats.setInputRecords(outputRecords);
            stats.setInputArtists(outputArtists);
            stats.setFilteredRecords(0L);
            stats.setFilteredArtists(0L);
            stats.setOutputRecords(outputRecords);
            stats.setOutputArtists(outputArtists);
        } else if (!outputExists) {
            // Output doesn't exist - set output stats to 0
            Long inputRecords = getRecordCount(inputTableName);
            Long inputArtists = getArtistCount(inputTableName);
            
            stats.setInputRecords(inputRecords);
            stats.setInputArtists(inputArtists);
            stats.setFilteredRecords(inputRecords);
            stats.setFilteredArtists(inputArtists);
            stats.setOutputRecords(0L);
            stats.setOutputArtists(0L);
        } else {
            // Both tables exist - normal case
            Long inputRecords = getRecordCount(inputTableName);
            Long inputArtists = getArtistCount(inputTableName);
            Long outputRecords = getRecordCount(outputTableName);
            Long outputArtists = getArtistCount(outputTableName);
            
            stats.setInputRecords(inputRecords);
            stats.setInputArtists(inputArtists);
            stats.setFilteredRecords(inputRecords - outputRecords);
            stats.setFilteredArtists(inputArtists - outputArtists);
            stats.setOutputRecords(outputRecords);
            stats.setOutputArtists(outputArtists);
        }
        
        return stats;
    }
    
    protected abstract String getStepSuffix();
    
    protected abstract StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun);
    
    protected Long getRecordCount(String tableName) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tableName)
            .getSingleResult()).longValue();
    }
    
    protected Long getArtistCount(String tableName) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + tableName)
            .getSingleResult()).longValue();
    }
    
    private void validateStep(Step step) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        if (!stepType.equals(step.getType())) {
            throw new IllegalArgumentException("Step type mismatch: expected " + stepType + ", got " + step.getType());
        }
    }
    
    private void validateInputTable(String inputTable) {
        if (inputTable == null || inputTable.trim().isEmpty()) {
            throw new IllegalArgumentException("Input table cannot be null or empty");
        }
        String[] parts = inputTable.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Input table must be in format 'schema.table'");
        }
    }
    
    private String generateOutputTableName(Step step, StepRun stepRun, @Nullable Long pipelineRunId) {
        // Get metadata from repository
        StepMetadataProjection metadata = stepRepository.getStepMetadata(step.getId());
        
        StringBuilder tableName = new StringBuilder();
        
        // Add prefix based on pipelineRunId
        if (pipelineRunId != null) {
            tableName.append("pr_");
        } else {
            tableName.append("sr_");
        }
        
        tableName.append("g").append(metadata.getGameId());
        tableName.append("_p").append(metadata.getPipelineId());

        if (pipelineRunId != null) {
            tableName.append("_pr").append(pipelineRunId);
        }

        tableName.append("_s").append(step.getId());
        tableName.append("_sr").append(stepRun.getId());

        // Add step-specific suffix
        String suffix = getStepSuffix();
        if (suffix != null && !suffix.isEmpty()) {
            tableName.append("_").append(suffix);
        }

        return "mu_quiz_stg." + tableName;
    }

    protected String generateAuxiliaryTableName(Step step, StepRun stepRun, String suffix) {
        String baseTableName = outputTableCache.get(stepRun.getId());
        if (baseTableName == null) {
            baseTableName = generateOutputTableName(step, stepRun, stepRun.getPipelineRunId());
            cacheOutputTableName(stepRun.getId(), baseTableName);
        }

        // Extract table name without schema
        String[] parts = baseTableName.split("\\.");
        String tableName = parts.length == 2 ? parts[1] : baseTableName;

        return "mu_quiz_stg." + tableName + "_" + suffix;
    }

    private void cacheOutputTableName(Long stepRunId, String outputTableName) {
        // Simple cache size management
        if (outputTableCache.size() >= MAX_CACHE_SIZE) {
            // Remove some entries (simple approach - remove first 100)
            outputTableCache.entrySet().stream()
                .limit(100)
                .map(Map.Entry::getKey)
                .forEach(outputTableCache::remove);
        }
        outputTableCache.put(stepRunId, outputTableName);
    }
}
