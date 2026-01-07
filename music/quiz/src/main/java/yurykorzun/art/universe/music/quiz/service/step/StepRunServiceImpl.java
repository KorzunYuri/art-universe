package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepRunServiceImpl implements StepRunService {

    private final StepRunRepository stepRunRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StepRun createStepRun(Step step, String inputTableName, @Nullable Long pipelineRunId) {
        StepRun stepRun = StepRun.builder()
            .pipelineRunId(pipelineRunId)
            .stepId(step.getId())
            .stepType(step.getType())
            .algVersion(step.getAlgVersion())
            .stepCfgData(step.getCfgData())
            .inputTableName(inputTableName)
            .status(ExecutionStatus.PENDING)
            .build();
        
        return stepRunRepository.save(stepRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StepRun updateStepRunStatus(Long stepRunId, ExecutionStatus status) {
        StepRun stepRun = stepRunRepository.findById(stepRunId)
            .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));
        
        stepRun.setStatus(status);
        stepRun.setStartedAt(Instant.now());
        return stepRunRepository.save(stepRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StepRun completeStepRun(Long stepRunId, String resultTableName, Long stepId) {
        StepRun stepRun = stepRunRepository.findById(stepRunId)
            .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));

        stepRun.setResultTableName(resultTableName);
        stepRun.setStatus(ExecutionStatus.COMPLETED);
        stepRun.setCompletedAt(Instant.now());
        return stepRunRepository.save(stepRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StepRun failStepRun(Long stepRunId) {
        StepRun stepRun = stepRunRepository.findById(stepRunId)
            .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));
        
        stepRun.setStatus(ExecutionStatus.FAILED);
        stepRun.setCompletedAt(Instant.now());
        return stepRunRepository.save(stepRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StepRun setResultStats(Long stepRunId, StepRunStats stats) {
        StepRun stepRun = stepRunRepository.findById(stepRunId)
            .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));

        try {
            String statsJson = objectMapper.writeValueAsString(stats);
            stepRun.setResultStats(statsJson);
            stepRun = stepRunRepository.save(stepRun);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize stats for step {}", stepRunId, e);
        }

        return stepRun;
    }
}
