package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StepExecutionServiceImpl implements StepExecutionService {

    private final StepRunRepository stepRunRepository;
    private final StepRepository stepRepository;
    private final ObjectMapper objectMapper;
    private final StepProcessorRegistry stepProcessorRegistry;

    @Override
    public String getPreview(Step step) {
        StepProcessor processor = stepProcessorRegistry.get(step.getType());
        return processor.getPreview(step);
    }

    @Override
    public StepRun executeStep(Step step, String inputTableName, @Nullable Long pipelineRunId) {
        StepProcessor processor = stepProcessorRegistry.get(step.getType());
        validateStep(step, processor);
        validateInputTable(inputTableName);

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
        String stepTableNameBase = generateStepTableNameBase(step, savedStepRun, pipelineRunId);

        // Update status to STARTED
        savedStepRun.setStatus(ExecutionStatus.STARTED);
        savedStepRun.setStartedAt(Instant.now());
        savedStepRun.setResultTableName(stepTableNameBase);
        stepRunRepository.save(savedStepRun);
        
        try {
            // Measure execution time
            long startTime = System.currentTimeMillis();
            
            // Call actual step processing procedure
            StepRunResult result = processor.processStep(step, inputTableName, stepTableNameBase, savedStepRun);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Update output table name from result
            savedStepRun.setResultTableName(result.getOutputTableName());
            
            // Calculate statistics
            StepRunStats stats = getResultStats(processor, stepRun);
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

        return "mu_quiz_stg." + tableName;
    }
}
