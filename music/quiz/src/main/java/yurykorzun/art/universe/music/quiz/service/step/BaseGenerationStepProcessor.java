package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.lang.Nullable;
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
    
    // Cache for output table names with simple LRU behavior
    private static final ConcurrentMap<Long, String> outputTableCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;
    
    protected BaseGenerationStepProcessor(StepType stepType, StepRunRepository stepRunRepository, StepRepository stepRepository) {
        this.stepType = stepType;
        this.stepRunRepository = stepRunRepository;
        this.stepRepository = stepRepository;
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
     * Processes a step and returns the output table name.
     * During execution, the Step entity will be saved and its lastStepRunId may be updated.
     */
    @Override
    public String process(Step step, String inputTableName, @Nullable Long pipelineRunId) {
        validateStep(step);
        validateInputTable(inputTableName);
        
        // Create StepRun
        StepRun stepRun = StepRun.builder()
            .pipelineRunId(pipelineRunId)
            .stepId(step.getId())
            .stepType(step.getType())
            .algVersion(step.getAlgVersion())
            .stepCfgData(step.getCfgData())
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
            // Call actual step processing procedure
            String resultStats = processStep(step, inputTableName, outputTableName, savedStepRun);
            
            // Mark as completed with result stats
            savedStepRun.setStatus(ExecutionStatus.COMPLETED);
            savedStepRun.setCompletedAt(Instant.now());
            savedStepRun.setResultStats(resultStats);
            stepRunRepository.save(savedStepRun);
            
            // Update step's lastStepRunId
            step.setLastStepRunId(savedStepRun.getId());
            stepRepository.save(step);
            
            return outputTableName;
            
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

    
    protected abstract String getStepSuffix();
    
    protected abstract String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun);
    
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
