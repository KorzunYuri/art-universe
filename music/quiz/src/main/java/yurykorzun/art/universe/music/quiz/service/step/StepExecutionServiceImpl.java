package yurykorzun.art.universe.music.quiz.service.step;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.service.StepService;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

@Service
@RequiredArgsConstructor
public class StepExecutionServiceImpl implements StepExecutionService {

    private final StepService stepService;
    private final StepProcessorRegistry stepProcessorRegistry;
    private final StepRunService stepRunService;

    @Override
    public String generatePreview(Long stepId) {
        return generatePreview(stepService.getStep(stepId));
    }

    @Override
    public String generatePreview(Step step) {
        // TODO cache preview
        StepProcessor processor = stepProcessorRegistry.get(step.getType());
        String preview = processor.getPreview(step);

        step.setPreviewData(preview);
        stepService.updatePreview(step.getId(), preview);

        return preview;
    }

    @Override
    public StepRun executeStep(Step step, String inputTableName, @Nullable Long pipelineRunId) {
        // TODO cache results
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        StepProcessor processor = stepProcessorRegistry.get(step.getType());
        validateStep(step, processor);
        validateInputTable(inputTableName);

        // Create StepRun in separate transaction
        StepRun savedStepRun = stepRunService.createStepRun(step, inputTableName, pipelineRunId);
        
        // Generate output table name and cache it
        String stepTableNameBase = generateStepTableNameBase(step, savedStepRun, pipelineRunId);

        // Update status to STARTED in separate transaction
        stepRunService.updateStepRunStatus(savedStepRun.getId(), ExecutionStatus.STARTED);
        
        try {
            // Measure execution time
            long startTime = System.currentTimeMillis();
            
            // Call actual step processing procedure
            StepRunResult result = processor.processStep(step, inputTableName, stepTableNameBase, savedStepRun);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Calculate statistics
            savedStepRun.setInputTableName(inputTableName);
            savedStepRun.setResultTableName(result.getOutputTableName());
            StepRunStats stats = getResultStats(processor, savedStepRun);
            stats.setExecutionTimeMs(executionTime);
            
            // Complete step run in separate transaction
            stepRunService.completeStepRun(savedStepRun.getId(), result.getOutputTableName(), stats, step.getId());
            
            // Return updated step run
            savedStepRun.setStatus(ExecutionStatus.COMPLETED);
            savedStepRun.setResultTableName(result.getOutputTableName());
            return savedStepRun;
            
        } catch (Exception e) {
            // Mark as failed in separate transaction
            stepRunService.failStepRun(savedStepRun.getId());
            throw new RuntimeException("Step processing failed", e);
        }
    }

    @Override
    public StepRunStats getResultStats(StepRun stepRun) {
        return getResultStats(stepProcessorRegistry.get(stepRun.getStepType()), stepRun);
    }

    private StepRunStats getResultStats(StepProcessor processor, StepRun stepRun) {
        return processor.getResultStats(stepRun);
    }

    private void validateStep(Step step, StepProcessor processor) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        if (!processor.getStepType().equals(step.getType())) {
            throw new IllegalArgumentException("Step type mismatch: expected " + processor.getStepType() + ", got " + step.getType());
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
    
    private String generateStepTableNameBase(Step step, StepRun stepRun, @Nullable Long pipelineRunId) {
        // Get metadata from repository
        StepMetadataProjection metadata = stepService.getStepMetadata(step.getId());
        
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

        return "mu_quiz_stg." + tableName;
    }
}
