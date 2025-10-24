package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepPosition;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.PipelineService;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessorRegistry;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineServiceImpl implements PipelineService {

    private final PipelineRepository pipelineRepository;
    private final StepRepository stepRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final StepRunRepository stepRunRepository;

    @Override
    @Transactional
    public PipelineDto createBasicPipeline() {
        log.debug("Creating basic pipeline");
        
        Pipeline pipeline = Pipeline.builder()
            .immutable(false)
            .build();
        
        Pipeline savedPipeline = pipelineRepository.save(pipeline);
        
        return mapToDto(savedPipeline, List.of());
    }

    @Override
    @Transactional
    public PipelineDto addStep(Long pipelineId, PipelineStepDto stepDto, Integer position) {
        log.debug("Adding step {} to pipeline {} at position {}", stepDto.getType(), pipelineId, position);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        validateStepPosition(stepDto.getType());
        
        GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(stepDto.getType());
        processor.validateConfiguration(stepDto.getCfgData());
        
        Step step = Step.builder()
            .type(stepDto.getType())
            .algVersion(stepDto.getType().getVersion())
            .cfgData(stepDto.getCfgData())
            .deleted(false)
            .immutable(false)
            .build();
        
        Step savedStep = stepRepository.save(step);
        
        // Shift existing steps using batch operation
        pipelineStepRepository.incrementOrderAfter(pipeline.getId(), position);
        
        PipelineStep pipelineStep = PipelineStep.builder()
            .pipelineId(pipeline.getId())
            .stepId(savedStep.getId())
            .ord(position)
            .build();
        
        pipelineStepRepository.save(pipelineStep);
        
        // Clear results for subsequent steps
        clearSubsequentStepResults(pipeline.getId(), position);
        
        return getPipeline(pipelineId);
    }

    @Override
    @Transactional
    public PipelineDto moveStep(Long pipelineId, Long stepId, Integer newPosition) {
        log.debug("Moving step {} to position {} in pipeline {}", stepId, newPosition, pipelineId);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
        
        PipelineStep movingStep = pipelineSteps.stream()
            .filter(ps -> ps.getStepId().equals(stepId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Step not found in pipeline"));
        
        Integer oldPosition = movingStep.getOrd();
        
        if (oldPosition.equals(newPosition)) {
            return getPipeline(pipelineId); // No change needed
        }
        
        // Reorder steps using batch operations
        if (oldPosition < newPosition) {
            // Moving down: decrement steps between old and new position
            pipelineStepRepository.decrementOrderBetween(pipelineId, oldPosition, newPosition);
        } else {
            // Moving up: increment steps between new and old position
            pipelineStepRepository.incrementOrderBetween(pipelineId, newPosition, oldPosition);
        }
        
        // Update the moving step to new position
        pipelineStepRepository.updateStepOrder(pipelineId, stepId, newPosition);
        
        // Clear results from earliest affected position
        Integer earliestPosition = Math.min(oldPosition, newPosition);
        clearSubsequentStepResults(pipeline.getId(), earliestPosition);
        
        return getPipeline(pipelineId);
    }

    @Override
    @Transactional
    public PipelineDto removeStep(Long pipelineId, Long stepId) {
        log.debug("Removing step {} from pipeline {}", stepId, pipelineId);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
        
        PipelineStep removingStep = pipelineSteps.stream()
            .filter(ps -> ps.getStepId().equals(stepId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Step not found in pipeline"));
        
        Integer removedPosition = removingStep.getOrd();
        
        // Remove from pipeline_step
        pipelineStepRepository.deleteByPipelineIdAndStepId(pipeline.getId(), stepId);
        
        // Mark step as deleted
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new IllegalArgumentException("Step not found"));
        step.setDeleted(true);
        stepRepository.save(step);
        
        // Shift subsequent steps
        pipelineStepRepository.decrementOrderAfter(pipeline.getId(), removedPosition);
        
        // Clear results for subsequent steps
        clearSubsequentStepResults(pipeline.getId(), removedPosition);
        
        return getPipeline(pipelineId);
    }

    @Override
    @Transactional
    public PipelineDto updateStepConfiguration(Long pipelineId, Long stepId, PipelineStepDto stepDto) {
        log.debug("Updating configuration for step {} in pipeline {}", stepId, pipelineId);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new IllegalArgumentException("Step not found"));
        
        GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(step.getType());
        processor.validateConfiguration(stepDto.getCfgData());
        
        boolean configChanged = !stepDto.getCfgData().equals(step.getCfgData());
        
        step.setCfgData(stepDto.getCfgData());
        stepRepository.save(step);
        
        if (configChanged) {
            // Find step position and clear subsequent results
            List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
            Integer stepPosition = pipelineSteps.stream()
                .filter(ps -> ps.getStepId().equals(stepId))
                .findFirst()
                .map(PipelineStep::getOrd)
                .orElseThrow(() -> new IllegalArgumentException("Step not found in pipeline"));
            
            clearSubsequentStepResults(pipeline.getId(), stepPosition);
        }
        
        return getPipeline(pipelineId);
    }

    @Override
    @Transactional
    public String getStepPreview(Long stepId) {
        log.debug("Getting preview for step {}", stepId);
        
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new IllegalArgumentException("Step not found"));
        
        GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(step.getType());
        String preview = processor.getPreview(step.getCfgData());
        
        step.setPreviewData(preview);
        stepRepository.save(step);
        
        return preview;
    }

    @Override
    @Transactional
    public StepRun executeStep(Long pipelineId, Long stepId) {
        log.debug("Executing step {} in pipeline {}", stepId, pipelineId);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId);
        
        // Find target step position
        Integer targetPosition = pipelineSteps.stream()
            .filter(ps -> ps.getStepId().equals(stepId))
            .findFirst()
            .map(PipelineStep::getOrd)
            .orElseThrow(() -> new IllegalArgumentException("Step not found in pipeline"));
        
        // Find earliest step without result
        Integer minPosition = findEarliestStepWithoutResult(pipelineId);
        if (minPosition > targetPosition) {
            minPosition = targetPosition;
        }
        
        // Clear results for steps after minPosition
        clearSubsequentStepResults(pipeline.getId(), minPosition);
        
        // Execute steps from minPosition to targetPosition
        return executeStepsInRange(pipelineSteps, minPosition, targetPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePipelineForGeneration(Long pipelineId) {
        log.debug("Validating pipeline for generation {}", pipelineId);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
        
        if (pipelineSteps.isEmpty()) {
            throw new IllegalArgumentException("Pipeline must have at least one step");
        }
        
        List<Long> stepIds = pipelineSteps.stream().map(PipelineStep::getStepId).toList();
        List<Step> steps = stepRepository.findAllById(stepIds);
        
        long startSteps = steps.stream()
            .filter(step -> getStepPosition(step.getType()) == GenerationStepPosition.START)
            .count();
        
        long finalSteps = steps.stream()
            .filter(step -> getStepPosition(step.getType()) == GenerationStepPosition.FINAL)
            .count();
        
        if (startSteps != 1) {
            throw new IllegalArgumentException("Pipeline must have exactly one START step");
        }
        
        if (finalSteps != 1) {
            throw new IllegalArgumentException("Pipeline must have exactly one FINAL step");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineDto getPipeline(Long pipelineId) {
        log.debug("Getting pipeline {}", pipelineId);
        
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
            .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + pipelineId));
        
        List<PipelineStepRepository.PipelineStepWithDetails> stepDetails = 
            pipelineStepRepository.findPipelineStepsWithDetails(pipelineId);
        
        List<PipelineStepDto> stepDtos = stepDetails.stream()
            .map(detail -> mapStepToDto(detail.getStep(), detail.getOrd(), detail.getStepRun()))
            .toList();
        
        return mapToDto(pipeline, stepDtos);
    }

    private Pipeline getPipelineById(Long pipelineId) {
        return pipelineRepository.findById(pipelineId)
            .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + pipelineId));
    }

    private void validateStepPosition(GenerationStepType stepType) {
        // This method would validate that step type matches allowed position
        // Implementation depends on business rules
    }

    private GenerationStepPosition getStepPosition(GenerationStepType stepType) {
        return switch (stepType) {
            case START_DATASOURCE -> GenerationStepPosition.START;
            case APPROVED_FILTER, BLACKLIST_FILTER, WHITELIST_FILTER, 
                 TRACK_RECENCY_PENALTY, ARTIST_RECENCY_PENALTY, ARTIST_DIVERSITY -> GenerationStepPosition.MIDDLE;
            case FINAL_SELECTION, FINAL_CATEGORIES_BALANCER -> GenerationStepPosition.FINAL;
        };
    }

    private void clearSubsequentStepResults(Long pipelineId, Integer fromPosition) {
        stepRepository.clearSubsequentStepResults(pipelineId, fromPosition);
    }

    private Integer findEarliestStepWithoutResult(Long pipelineId) {
        return pipelineStepRepository.findEarliestStepWithoutResult(pipelineId)
            .orElse(1); // Default to position 1 if no steps or all have results
    }

    @Override
    @Transactional
    public PipelineRun executePipeline(Long pipelineId, Long pipelineRunId) {
        log.debug("Executing pipeline {} with run {}", pipelineId, pipelineRunId);
        
        PipelineRun pipelineRun = pipelineRunRepository.findById(pipelineRunId)
            .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found: " + pipelineRunId));
        
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId);
        if (pipelineSteps.isEmpty()) {
            throw new IllegalArgumentException("Pipeline has no steps");
        }
        
        pipelineRun.setStatus(ExecutionStatus.STARTED);
        pipelineRun.setStartedAt(Instant.now());
        pipelineRunRepository.save(pipelineRun);
        
        try {
            String currentTable = "mu_view.v_track";
            
            for (PipelineStep pipelineStep : pipelineSteps) {
                Step step = stepRepository.findById(pipelineStep.getStepId())
                    .orElseThrow(() -> new IllegalArgumentException("Step not found: " + pipelineStep.getStepId()));
                
                GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(step.getType());
                currentTable = processor.process(step, currentTable, pipelineRunId);
            }
            
            pipelineRun.setStatus(ExecutionStatus.COMPLETED);
            pipelineRun.setCompletedAt(Instant.now());
            pipelineRun.setResultTableName(currentTable);
            
        } catch (Exception e) {
            pipelineRun.setStatus(ExecutionStatus.FAILED);
            pipelineRun.setCompletedAt(Instant.now());
            throw new RuntimeException("Pipeline execution failed", e);
        } finally {
            pipelineRunRepository.save(pipelineRun);
        }
        
        return pipelineRun;
    }

    private StepRun executeStepsInRange(List<PipelineStep> pipelineSteps, Integer fromPosition, Integer toPosition) {
        String currentTable = "mu_view.v_track";
        StepRun lastStepRun = null;
        
        for (PipelineStep pipelineStep : pipelineSteps) {
            if (pipelineStep.getOrd() < fromPosition || pipelineStep.getOrd() > toPosition) {
                continue;
            }
            
            // If not the first step, get input table from previous step
            if (pipelineStep.getOrd() > 1) {
                PipelineStep previousStep = pipelineSteps.stream()
                    .filter(ps -> ps.getOrd().equals(pipelineStep.getOrd() - 1))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Previous step not found"));
                
                Step prevStep = stepRepository.findById(previousStep.getStepId())
                    .orElseThrow(() -> new IllegalArgumentException("Previous step entity not found"));
                
                if (prevStep.getLastStepRunId() != null) {
                    StepRun prevStepRun = stepRunRepository.findById(prevStep.getLastStepRunId())
                        .orElseThrow(() -> new IllegalArgumentException("Previous step run not found"));
                    currentTable = prevStepRun.getResultTableName();
                }
            }
            
            Step step = stepRepository.findById(pipelineStep.getStepId())
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + pipelineStep.getStepId()));
            
            GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(step.getType());
            currentTable = processor.process(step, currentTable, null); // null = not pipeline run
            
            // Get the last step run for return
            if (step.getLastStepRunId() != null) {
                lastStepRun = stepRunRepository.findById(step.getLastStepRunId())
                    .orElse(null);
            }
        }
        
        return lastStepRun;
    }

    private PipelineDto mapToDto(Pipeline pipeline, List<PipelineStepDto> steps) {
        return PipelineDto.builder()
            .id(pipeline.getId())
            .immutable(pipeline.getImmutable())
            .steps(steps)
            .build();
    }

    private PipelineStepDto mapStepToDto(Step step, Integer ord, StepRun stepRun) {
        return PipelineStepDto.builder()
            .id(step.getId())
            .type(step.getType())
            .algVersion(step.getAlgVersion())
            .cfgData(step.getCfgData())
            .previewData(step.getPreviewData())
            .resultTableName(stepRun != null ? stepRun.getResultTableName() : null)
            .resultStats(stepRun != null ? stepRun.getResultStats() : null)
            .ord(ord)
            .build();
    }
}
