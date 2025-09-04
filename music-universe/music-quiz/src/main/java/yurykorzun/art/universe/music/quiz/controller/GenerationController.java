package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.quiz.dto.CreateGenerationRequest;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.service.GenerationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping("/games/{gameId}/generations")
    public GenerationDto generateTracks(@PathVariable Long gameId, @RequestBody CreateGenerationRequest request) {
        log.info("Generating tracks for game {} with target count {}", gameId, request.getTargetCount());
        return generationService.generateTracks(gameId, request.getTargetCount());
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
}
