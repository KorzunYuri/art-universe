package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.PipelineService;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessorRegistry;
import yurykorzun.art.universe.music.quiz.util.PipelineValidationUtil;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId);
        
        // Get existing step types for validation
        List<StepType> existingStepTypes = getStepTypes(pipelineSteps);
        PipelineValidationUtil.validateStepPosition(stepDto.getType(), position, existingStepTypes);
        
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
        
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new IllegalArgumentException("Step not found"));
        
        List<StepType> allStepTypes = getStepTypes(pipelineSteps);
        PipelineValidationUtil.validateStepPositionForMove(step.getType(), newPosition, allStepTypes);
        
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
        
        PipelineValidationUtil.validatePipelineForGeneration(steps);
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

    private List<StepType> getStepTypes(List<PipelineStep> pipelineSteps) {
        List<Long> stepIds = pipelineSteps.stream().map(PipelineStep::getStepId).toList();
        List<Step> steps = stepRepository.findAllById(stepIds);
        Map<Long, StepType> stepTypeMap = steps.stream()
            .collect(Collectors.toMap(Step::getId, Step::getType));
        
        return pipelineSteps.stream()
            .map(ps -> stepTypeMap.get(ps.getStepId()))
            .toList();
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
            String currentTable = null;
            
            for (PipelineStep pipelineStep : pipelineSteps) {
                Step step = stepRepository.findById(pipelineStep.getStepId())
                    .orElseThrow(() -> new IllegalArgumentException("Step not found: " + pipelineStep.getStepId()));
                
                GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(step.getType());
                StepRun stepRun = processor.process(step, currentTable, pipelineRunId);
                currentTable = stepRun.getResultTableName();
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
        String currentTable = null;
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
            StepRun stepRun = processor.process(step, currentTable, null); // null = not pipeline run
            currentTable = stepRun.getResultTableName();
            
            // Get the last step run for return
            lastStepRun = stepRun;
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
