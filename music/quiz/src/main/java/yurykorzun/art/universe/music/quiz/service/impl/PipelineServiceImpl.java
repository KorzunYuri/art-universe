package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;
import yurykorzun.art.universe.music.quiz.repository.PipelineStepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.PipelineRunService;
import yurykorzun.art.universe.music.quiz.service.PipelineService;
import yurykorzun.art.universe.music.quiz.service.StepService;
import yurykorzun.art.universe.music.quiz.service.step.StepExecutionService;
import yurykorzun.art.universe.music.quiz.util.PipelineValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineServiceImpl implements PipelineService {

    private final PipelineRepository pipelineRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final StepRunRepository stepRunRepository;
    private final StepExecutionService stepExecutionService;
    private final PipelineRunService pipelineRunService;
    private final StepService stepService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineDto createBasicPipeline() {
        log.debug("Creating basic pipeline");
        
        Pipeline pipeline = Pipeline.builder()
            .immutable(false)
            .build();
        
        Pipeline savedPipeline = pipelineRepository.save(pipeline);
        
        return mapToDto(savedPipeline, List.of());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pipeline createImmutableCopy(Long originalPipelineId) {
        log.debug("Creating immutable copy of pipeline {}", originalPipelineId);
        
        Pipeline originalPipeline = getPipelineById(originalPipelineId);
        
        // Create immutable pipeline
        Pipeline immutablePipeline = Pipeline.builder()
            .immutable(true)
            .build();
        Pipeline savedPipeline = pipelineRepository.save(immutablePipeline);
        
        // Copy pipeline steps and steps
        List<PipelineStep> originalSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(originalPipelineId);
        List<PipelineStep> immutablePipelineSteps = new ArrayList<>();
        for (PipelineStep originalPipelineStep : originalSteps) {
            // Create immutable copy of step
            Step immutableStep = stepService.createImmutableCopy(originalPipelineStep.getStepId());
            
            // Create pipeline step for immutable pipeline
            PipelineStep immutablePipelineStep = PipelineStep.builder()
                .pipelineId(savedPipeline.getId())
                .stepId(immutableStep.getId())
                .ord(originalPipelineStep.getOrd())
                .build();
            immutablePipelineSteps.add(immutablePipelineStep);
        }
        pipelineStepRepository.saveAll(immutablePipelineSteps);
        
        return savedPipeline;
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
        
        // Create step using StepService
        Step savedStep = stepService.createStep(stepDto.getType(), stepDto.getCfgData());
        
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
        
        Step step = stepService.getStep(stepId);
        Integer oldPosition = movingStep.getOrd();

        List<StepType> allStepTypes = getStepTypes(pipelineSteps);
        PipelineValidationUtil.validateStepPositionForMove(step.getType(), oldPosition, newPosition, allStepTypes);
        
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
        
        // Mark step as deleted using StepService
        stepService.softDeleteStep(stepId);
        
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
        Step step = stepService.getStep(stepId);
        
        boolean configChanged = !stepDto.getCfgData().equals(step.getCfgData());
        
        stepService.updateStepConfiguration(stepId, stepDto.getCfgData());
        
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
        return stepExecutionService.generatePreview(stepId);
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
        List<Step> steps = stepService.getSteps(stepIds);
        
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
        List<Step> steps = stepService.getSteps(stepIds);
        Map<Long, StepType> stepTypeMap = steps.stream()
            .collect(Collectors.toMap(Step::getId, Step::getType));
        
        return pipelineSteps.stream()
            .map(ps -> stepTypeMap.get(ps.getStepId()))
            .toList();
    }

    private void clearSubsequentStepResults(Long pipelineId, Integer fromPosition) {
        List<Long> stepIds = pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)
            .stream()
            .filter(ps -> ps.getOrd() >= fromPosition)
            .map(PipelineStep::getStepId)
            .toList();
        
        stepService.clearResults(stepIds);
    }

    private Integer findEarliestStepWithoutResult(Long pipelineId) {
        return pipelineStepRepository.findEarliestStepWithoutResult(pipelineId)
            .orElse(0); // Default to position 0 if no steps or all have results
    }

    @Override
    @Transactional
    public PipelineRun executePipeline(Long pipelineId) {
        log.debug("Executing pipeline {}", pipelineId);
        
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId);
        if (pipelineSteps.isEmpty()) {
            throw new IllegalArgumentException("Pipeline has no steps");
        }
        List<Long> stepIds = pipelineSteps.stream()
            .map(PipelineStep::getStepId)
            .toList();
        Map<Long, Step> stepMap = stepService.getSteps(stepIds).stream()
            .collect(Collectors.toMap(Step::getId, Function.identity()));
        
        PipelineRun pipelineRun = pipelineRunService.createPipelineRun(pipelineId);
        Long pipelineRunId = pipelineRun.getId();
        
        pipelineRunService.startPipelineRun(pipelineRunId);
        
        try {
            String currentTable = null;

            List<Step> stepsToExecute = new ArrayList<>();
            for (PipelineStep pipelineStep : pipelineSteps) {
                Step step = stepMap.get(pipelineStep.getStepId());
                stepsToExecute.add(step);
            }

            for (Step step : stepsToExecute) {
                StepRun stepRun = stepExecutionService.executeStep(step, currentTable, pipelineRunId);
                stepService.updateLastRun(step.getId(), stepRun.getId());
                currentTable = stepRun.getResultTableName();
            }

            pipelineRunService.completePipelineRun(pipelineRunId, currentTable);

        } catch (Exception e) {
            pipelineRunService.failPipelineRun(pipelineRunId);
            throw new RuntimeException("Pipeline execution failed", e);
        }
        
        return pipelineRun;
    }

    // pipeline steps are already sorted by ord
    private StepRun executeStepsInRange(List<PipelineStep> pipelineSteps, Integer fromPosition, Integer toPosition) {
        // Get step IDs for batch loading (including previous step for input table)
        List<Long> stepIds = pipelineSteps.stream()
            .filter(ps -> ps.getOrd() >= Math.max(0, fromPosition - 1) && ps.getOrd() <= toPosition)
            .map(PipelineStep::getStepId)
            .toList();
        
        // Batch load all needed steps
        List<Step> steps = stepService.getSteps(stepIds);
        Map<Long, Step> stepMap = steps.stream()
            .collect(Collectors.toMap(Step::getId, step -> step));
        
        // Get input table from previous step (if exists)
        String currentTable = null;
        if (fromPosition > 0) {
            PipelineStep inputPipelineStep = pipelineSteps.get(fromPosition - 1);
            Step inputStep = stepMap.get(inputPipelineStep.getStepId());
            if (inputStep != null && inputStep.getLastStepRunId() != null) {
                StepRun inputRun = stepRunRepository.findById(inputStep.getLastStepRunId())
                    .orElseThrow(() -> new IllegalArgumentException("Input run not found with id " + inputStep.getLastStepRunId()));
                currentTable = inputRun.getResultTableName();
            }
        }

        // Execute steps in order (pipelineSteps is already sorted by ord)
        StepRun lastStepRun = null;
        List<PipelineStep> stepsToExecute = pipelineSteps.stream()
            .filter(ps -> ps.getOrd() >= fromPosition && ps.getOrd() <= toPosition)
            .toList();
        
        for (PipelineStep pipelineStep : stepsToExecute) {
            Step step = stepMap.get(pipelineStep.getStepId());
            if (step == null) {
                throw new IllegalArgumentException("Step not found: " + pipelineStep.getStepId());
            }
            
            StepRun stepRun = stepExecutionService.executeStep(step, currentTable, null);

            stepService.updateLastRun(step.getId(), stepRun.getId());

            currentTable = stepRun.getResultTableName();
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
