package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/games/{gameId}/pipeline")
    public PipelineDto getPipeline(@PathVariable Long gameId) {
        log.debug("Getting pipeline for game {}", gameId);
        return pipelineService.getPipeline(gameId);
    }

    @PostMapping("/games/{gameId}/pipeline/steps")
    public PipelineDto addStep(@PathVariable Long gameId, 
                              @RequestBody PipelineStepDto stepDto,
                              @RequestParam Integer position) {
        log.info("Adding step {} to pipeline for game {} at position {}", stepDto.getType(), gameId, position);
        return pipelineService.addStep(gameId, stepDto, position);
    }

    @PutMapping("/games/{gameId}/pipeline/steps/{stepId}/move")
    public PipelineDto moveStep(@PathVariable Long gameId,
                               @PathVariable Long stepId,
                               @RequestParam Integer newPosition) {
        log.info("Moving step {} to position {} in pipeline for game {}", stepId, newPosition, gameId);
        return pipelineService.moveStep(gameId, stepId, newPosition);
    }

    @DeleteMapping("/games/{gameId}/pipeline/steps/{stepId}")
    public PipelineDto removeStep(@PathVariable Long gameId, @PathVariable Long stepId) {
        log.info("Removing step {} from pipeline for game {}", stepId, gameId);
        return pipelineService.removeStep(gameId, stepId);
    }

    @PutMapping("/games/{gameId}/pipeline/steps/{stepId}")
    public PipelineDto updateStepConfiguration(@PathVariable Long gameId,
                                              @PathVariable Long stepId,
                                              @RequestBody PipelineStepDto stepDto) {
        log.info("Updating configuration for step {} in pipeline for game {}", stepId, gameId);
        return pipelineService.updateStepConfiguration(gameId, stepId, stepDto);
    }

    @GetMapping("/steps/{stepId}/preview")
    public String getStepPreview(@PathVariable Long stepId) {
        log.debug("Getting preview for step {}", stepId);
        return pipelineService.getStepPreview(stepId);
    }

    @PostMapping("/games/{gameId}/pipeline/steps/{stepId}/execute")
    public PipelineDto executeStep(@PathVariable Long gameId, @PathVariable Long stepId) {
        log.info("Executing step {} in pipeline for game {}", stepId, gameId);
        return pipelineService.executeStep(gameId, stepId);
    }
}
