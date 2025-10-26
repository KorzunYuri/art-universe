package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
public class StepRunServiceImpl implements StepRunService {

    private final StepRunRepository stepRunRepository;
    private final StepRepository stepRepository;
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
    public void updateStepRunStatus(Long stepRunId, ExecutionStatus status, String resultTableName) {
        StepRun stepRun = stepRunRepository.findById(stepRunId)
            .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));
        
        stepRun.setStatus(status);
        stepRun.setStartedAt(Instant.now());
        stepRun.setResultTableName(resultTableName);
        stepRunRepository.save(stepRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeStepRun(Long stepRunId, String resultTableName, StepRunStats stats, Long stepId) {
        try {
            StepRun stepRun = stepRunRepository.findById(stepRunId)
                .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));
            
            String statsJson = objectMapper.writeValueAsString(stats);
            
            stepRun.setResultTableName(resultTableName);
            stepRun.setResultStats(statsJson);
            stepRun.setStatus(ExecutionStatus.COMPLETED);
            stepRun.setCompletedAt(Instant.now());
            stepRunRepository.save(stepRun);
            
            // Update step's lastStepRunId
            Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));
            step.setLastStepRunId(stepRunId);
            stepRepository.save(step);
        } catch (Exception e) {
            throw new RuntimeException("Failed to complete step run", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failStepRun(Long stepRunId) {
        StepRun stepRun = stepRunRepository.findById(stepRunId)
            .orElseThrow(() -> new IllegalArgumentException("StepRun not found: " + stepRunId));
        
        stepRun.setStatus(ExecutionStatus.FAILED);
        stepRun.setCompletedAt(Instant.now());
        stepRunRepository.save(stepRun);
    }
}
