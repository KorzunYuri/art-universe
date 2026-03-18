package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping("/pipeline/{pipelineId}")
    public PipelineDto getPipeline(@PathVariable Long pipelineId) {
        log.debug("Getting pipeline {}", pipelineId);
        return pipelineService.getPipeline(pipelineId);
    }

    @PostMapping("/pipeline/{pipelineId}/steps")
    @PreAuthorize("hasRole('QUIZ_MASTER')")
    public PipelineDto addStep(@PathVariable Long pipelineId,
                              @RequestBody PipelineStepDto stepDto,
                              @RequestParam Integer position) {
        log.info("Adding step {} to pipeline {} at position {}", stepDto.getType(), pipelineId, position);
        return pipelineService.addStep(pipelineId, stepDto, position);
    }

    @PutMapping("/pipeline/{pipelineId}/steps/{stepId}/move")
    @PreAuthorize("hasRole('QUIZ_MASTER')")
    public PipelineDto moveStep(@PathVariable Long pipelineId,
                               @PathVariable Long stepId,
                               @RequestParam Integer newPosition) {
        log.info("Moving step {} to position {} in pipeline {}", stepId, newPosition, pipelineId);
        return pipelineService.moveStep(pipelineId, stepId, newPosition);
    }

    @DeleteMapping("/pipeline/{pipelineId}/steps/{stepId}")
    @PreAuthorize("hasRole('QUIZ_MASTER')")
    public PipelineDto removeStep(@PathVariable Long pipelineId, @PathVariable Long stepId) {
        log.info("Removing step {} from pipeline {}", stepId, pipelineId);
        return pipelineService.removeStep(pipelineId, stepId);
    }

    @PutMapping("/pipeline/{pipelineId}/steps/{stepId}")
    @PreAuthorize("hasRole('QUIZ_MASTER')")
    public PipelineDto updateStepConfiguration(@PathVariable Long pipelineId,
                                              @PathVariable Long stepId,
                                              @RequestBody PipelineStepDto stepDto) {
        log.info("Updating configuration for step {} in pipeline {}", stepId, pipelineId);
        return pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto);
    }

    @GetMapping("/steps/{stepId}/preview")
    public String getStepPreview(@PathVariable Long stepId) {
        log.debug("Getting preview for step {}", stepId);
        return pipelineService.getStepPreview(stepId);
    }

    @PostMapping("/pipeline/{pipelineId}/steps/{stepId}/execute")
    @PreAuthorize("hasRole('QUIZ_MASTER')")
    public PipelineDto executeStep(@PathVariable Long pipelineId, @PathVariable Long stepId) {
        log.info("Executing step {} in pipeline {}", stepId, pipelineId);
        pipelineService.executeStep(pipelineId, stepId);
        return pipelineService.getPipeline(pipelineId);
    }
}
