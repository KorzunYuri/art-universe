package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.lang.Nullable;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import java.time.Instant;

public abstract class BaseGenerationStepProcessor implements GenerationStepProcessor {
    
    private final GenerationStepType stepType;
    private final StepRunRepository stepRunRepository;
    private final StepRepository stepRepository;
    
    protected BaseGenerationStepProcessor(GenerationStepType stepType, StepRunRepository stepRunRepository, StepRepository stepRepository) {
        this.stepType = stepType;
        this.stepRunRepository = stepRunRepository;
        this.stepRepository = stepRepository;
        GenerationStepProcessorRegistry.register(this);
    }
    
    @Override
    public GenerationStepType getStepType() {
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
        
        // Generate output table name
        String outputTableName = generateOutputTableName(step, savedStepRun);
        
        // Update status to STARTED
        savedStepRun.setStatus(ExecutionStatus.STARTED);
        savedStepRun.setStartedAt(Instant.now());
        savedStepRun.setResultTableName(outputTableName);
        stepRunRepository.save(savedStepRun);
        
        try {
            // TODO: Call actual step processing procedure
            // String resultStats = processStep(step, inputTableName, outputTableName, savedStepRun);
            
            // For now, mark as completed with empty stats
            savedStepRun.setStatus(ExecutionStatus.COMPLETED);
            savedStepRun.setCompletedAt(Instant.now());
            savedStepRun.setResultStats("{}");
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
        // Default implementation - override if needed
    }
    
    @Override
    public String getPreview(String cfgData) {
        // Default implementation - override if needed
        return "{}";
    }

    protected String migrateConfiguration(String cfgData, Integer fromVersion, Integer toVersion) {
        // Default implementation - override if needed
        return cfgData;
    }
    
    protected abstract String getStepSuffix();
    
    // TODO: Implement actual step processing
    // protected abstract String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun);
    
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
    
    private String generateOutputTableName(Step step, StepRun stepRun) {
        // Get metadata from repository
        StepMetadataProjection metadata = stepRepository.getStepMetadata(step.getId());
        
        StringBuilder tableName = new StringBuilder();
        
        // Add context-specific
        final Long pipelineRunId = stepRun.getPipelineRunId();
        tableName.append(pipelineRunId != null ? "p_": "s_");
        
        tableName.append("g").append(metadata.getGameId());
        tableName.append("_p").append(metadata.getPipelineId());
        
        if (pipelineRunId != null) {
            tableName.append("_pr").append(pipelineRunId);
        }
        
        tableName.append("_s").append(step.getId());
        tableName.append("_sr").append(stepRun.getId());
        
        // Add step type-specific suffix
        String suffix = getStepSuffix();
        if (suffix != null && !suffix.isEmpty()) {
            tableName.append("_").append(suffix);
        }
        
        return "mu_quiz_stg." + tableName;
    }
}
