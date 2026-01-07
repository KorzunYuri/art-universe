package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.service.GenerationService;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class GenerationController {

    private final GenerationService generationService;
    private final PipelineService pipelineService;

    @PostMapping("/games/{gameId}/generations")
    public GenerationDto generateTracks(@PathVariable Long gameId) {
        log.info("Generating tracks for game {}", gameId);
        return generationService.generateTracks(gameId);
    }

    @GetMapping("/games/{gameId}/generations")
    public List<GenerationDto> getGenerations(@PathVariable Long gameId) {
        log.debug("Getting generations for game {}", gameId);
        return generationService.getGenerations(gameId);
    }

    @GetMapping("/generations/{generationId}/tracks")
    public List<GenerationTrackDto> getGenerationTracks(@PathVariable Long generationId) {
        log.debug("Getting tracks for generation {}", generationId);
        return generationService.getGenerationTracks(generationId);
    }

    @PatchMapping("/generations/{generationId}/approve")
    public GenerationDto approveGeneration(@PathVariable Long generationId) {
        log.info("Approving generation {}", generationId);
        return generationService.approveGeneration(generationId);
    }

    @PatchMapping("/generations/{generationId}/disapprove")
    public GenerationDto disapproveGeneration(@PathVariable Long generationId) {
        log.info("Disapproving generation {}", generationId);
        return generationService.disapproveGeneration(generationId);
    }

    @DeleteMapping("/generations/{generationId}/tracks/{trackId}")
    public void removeTrackFromGeneration(@PathVariable Long generationId, @PathVariable Long trackId) {
        log.info("Removing track {} from generation {}", trackId, generationId);
        generationService.removeTrackFromGeneration(generationId, trackId);
    }

    @GetMapping("/generations/{generationId}/pipeline")
    public PipelineDto getGenerationPipeline(@PathVariable Long generationId) {
        log.debug("Getting pipeline for generation {}", generationId);
        GenerationDto generation = generationService.getGenerations(null).stream()
            .filter(g -> g.getId().equals(generationId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Generation not found: " + generationId));
        
        if (generation.getPipelineId() == null) {
            throw new IllegalArgumentException("Generation has no associated pipeline");
        }
        
        return pipelineService.getPipeline(generation.getPipelineId());
    }
}
