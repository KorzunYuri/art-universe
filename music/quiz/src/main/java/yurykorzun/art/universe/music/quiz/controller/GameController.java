package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithPipelineDto;
import yurykorzun.art.universe.music.quiz.service.GameService;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    @PostMapping
    @PreAuthorize("hasRole('QUIZ_MASTER')")
    public GameWithPipelineDto createGame() {
        log.info("Creating new game");
        return gameService.createGame();
    }

    @GetMapping
    public Page<GameDto> getAllGames(Pageable pageable) {
        log.debug("Getting all games with pageable: {}", pageable);
        return gameService.getAllGames(pageable);
    }

    @GetMapping("/{gameId}")
    public GameWithPipelineDto getGame(@PathVariable Long gameId) {
        log.debug("Getting game {}", gameId);
        return gameService.getGame(gameId);
    }
}
