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

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        validateStepPosition(stepDto.getType());
        
        GenerationStepProcessor<?> processor = GenerationStepProcessorRegistry.get(stepDto.getType());
        processor.validateConfiguration(stepDto.getCfgData());
        
        Step step = Step.builder()
            .type(stepDto.getType())
            .algVersion(stepDto.getType().getVersion())
            .cfgData(stepDto.getCfgData())
            .deleted(false)
            .immutable(false)
            .build();
        
        Step savedStep = stepRepository.save(step);
        
        // Shift existing steps if needed
        List<PipelineStep> existingSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
        for (PipelineStep ps : existingSteps) {
            if (ps.getOrd() >= position) {
                ps.setOrd(ps.getOrd() + 1);
                pipelineStepRepository.save(ps);
            }
        }
        
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
        
        // Reorder steps
        for (PipelineStep ps : pipelineSteps) {
            if (ps.getStepId().equals(stepId)) {
                ps.setOrd(newPosition);
            } else if (oldPosition < newPosition && ps.getOrd() > oldPosition && ps.getOrd() <= newPosition) {
                ps.setOrd(ps.getOrd() - 1);
            } else if (oldPosition > newPosition && ps.getOrd() >= newPosition && ps.getOrd() < oldPosition) {
                ps.setOrd(ps.getOrd() + 1);
            }
            pipelineStepRepository.save(ps);
        }
        
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
        
        GenerationStepProcessor<?> processor = GenerationStepProcessorRegistry.get(step.getType());
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
        
        GenerationStepProcessor<?> processor = GenerationStepProcessorRegistry.get(step.getType());
        String preview = processor.getPreview(step.getCfgData());
        
        step.setPreviewData(preview);
        stepRepository.save(step);
        
        return preview;
    }

    @Override
    @Transactional
    public PipelineDto executeStep(Long pipelineId, Long stepId) {
        log.debug("Executing step {} in pipeline {}", stepId, pipelineId);
        
        Pipeline pipeline = getPipelineById(pipelineId);
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
        
        // Find earliest step without result
        Integer minPosition = findEarliestStepWithoutResult(pipelineSteps);
        
        // Clear results for steps after minPosition
        clearSubsequentStepResults(pipeline.getId(), minPosition);
        
        // Execute steps starting from minPosition
        executeStepsFromPosition(pipeline.getId(), pipelineSteps, minPosition);
        
        return getPipeline(pipelineId);
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
        
        List<PipelineStep> pipelineSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipeline.getId());
        List<Long> stepIds = pipelineSteps.stream().map(PipelineStep::getStepId).toList();
        
        Map<Long, Step> stepsMap = stepRepository.findAllById(stepIds).stream()
            .collect(Collectors.toMap(Step::getId, step -> step));
        
        // Get step runs for result data
        List<Long> stepRunIds = stepsMap.values().stream()
            .map(Step::getLastStepRunId)
            .filter(Objects::nonNull)
            .toList();
        
        Map<Long, StepRun> stepRunsMap = stepRunRepository.findAllById(stepRunIds).stream()
            .collect(Collectors.toMap(StepRun::getId, stepRun -> stepRun));
        
        List<PipelineStepDto> stepDtos = pipelineSteps.stream()
            .map(ps -> {
                Step step = stepsMap.get(ps.getStepId());
                StepRun stepRun = step.getLastStepRunId() != null ? stepRunsMap.get(step.getLastStepRunId()) : null;
                return mapStepToDto(step, ps.getOrd(), stepRun);
            })
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
        List<PipelineStep> subsequentSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)
            .stream()
            .filter(ps -> ps.getOrd() >= fromPosition)
            .toList();
        
        for (PipelineStep ps : subsequentSteps) {
            Step step = stepRepository.findById(ps.getStepId()).orElse(null);
            if (step != null) {
                step.setPreviewData(null);
                step.setLastStepRunId(null);
                stepRepository.save(step);
            }
        }
    }

    private Integer findEarliestStepWithoutResult(List<PipelineStep> pipelineSteps) {
        for (PipelineStep ps : pipelineSteps) {
            Step step = stepRepository.findById(ps.getStepId()).orElse(null);
            if (step == null || step.getLastStepRunId() == null) {
                return ps.getOrd();
            }
        }
        return pipelineSteps.isEmpty() ? 1 : pipelineSteps.getLast().getOrd();
    }

    private void executeStepsFromPosition(Long pipelineId, List<PipelineStep> pipelineSteps, Integer fromPosition) {
        // This method would execute steps starting from the given position
        // Implementation would involve calling step processors
        // For now, leaving as placeholder
        log.debug("Executing steps from position {} for pipeline {}", fromPosition, pipelineId);
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
