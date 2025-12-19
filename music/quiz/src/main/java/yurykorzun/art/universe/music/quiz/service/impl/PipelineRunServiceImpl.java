package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.PipelineRun;
import yurykorzun.art.universe.music.quiz.repository.PipelineRunRepository;
import yurykorzun.art.universe.music.quiz.service.PipelineRunService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PipelineRunServiceImpl implements PipelineRunService {

    private final PipelineRunRepository pipelineRunRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineRun createPipelineRun(Long pipelineId) {
        PipelineRun pipelineRun = PipelineRun.builder()
            .pipelineId(pipelineId)
            .build();
        return pipelineRunRepository.save(pipelineRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineRun startPipelineRun(Long pipelineRunId) {
        PipelineRun pipelineRun = pipelineRunRepository.findById(pipelineRunId)
            .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found: " + pipelineRunId));
        
        pipelineRun.setStatus(ExecutionStatus.STARTED);
        pipelineRun.setStartedAt(Instant.now());
        return pipelineRunRepository.save(pipelineRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineRun completePipelineRun(Long pipelineRunId, String resultTableName) {
        PipelineRun pipelineRun = pipelineRunRepository.findById(pipelineRunId)
            .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found: " + pipelineRunId));
        
        pipelineRun.setStatus(ExecutionStatus.COMPLETED);
        pipelineRun.setCompletedAt(Instant.now());
        pipelineRun.setResultTableName(resultTableName);
        return pipelineRunRepository.save(pipelineRun);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineRun failPipelineRun(Long pipelineRunId) {
        PipelineRun pipelineRun = pipelineRunRepository.findById(pipelineRunId)
            .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found: " + pipelineRunId));
        
        pipelineRun.setStatus(ExecutionStatus.FAILED);
        pipelineRun.setCompletedAt(Instant.now());
        return pipelineRunRepository.save(pipelineRun);
    }
}
