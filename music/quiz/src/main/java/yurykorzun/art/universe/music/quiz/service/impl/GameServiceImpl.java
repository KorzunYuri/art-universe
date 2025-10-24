package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithGenerationsDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithPipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.entity.Game;
import yurykorzun.art.universe.music.quiz.repository.GameRepository;
import yurykorzun.art.universe.music.quiz.service.GameService;
import yurykorzun.art.universe.music.quiz.service.GenerationService;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final GenerationService generationService;
    private final PipelineService pipelineService;

    @Override
    @Transactional
    public GameWithPipelineDto createGame() {
        log.debug("Creating new game");
        
        // Create pipeline first to get its ID
        Game tempGame = Game.builder().pipelineId(-1L).build(); // temporary
        Game savedGame = gameRepository.save(tempGame);
        
        // Create pipeline for the game
        PipelineDto pipeline = pipelineService.createBasicPipeline(savedGame.getId());
        
        // Update game with pipeline ID
        savedGame.setPipelineId(pipeline.getId());
        savedGame = gameRepository.save(savedGame);
        
        log.debug("Created game with id: {} and pipeline id: {}", savedGame.getId(), pipeline.getId());
        
        return GameWithPipelineDto.builder()
            .id(savedGame.getId())
            .createdAt(savedGame.getCreatedAt())
            .pipeline(pipeline)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDto> getAllGames(Pageable pageable) {
        log.debug("Getting all games with pageable: {}", pageable);
        
        return gameRepository.findAll(pageable)
            .map(game -> GameDto.builder()
                .id(game.getId())
                .createdAt(game.getCreatedAt())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public GameWithGenerationsDto getGameWithGenerations(Long gameId) {
        log.debug("Getting game {} with generations", gameId);
        
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        return GameWithGenerationsDto.builder()
            .id(game.getId())
            .createdAt(game.getCreatedAt())
            .generations(generationService.getGenerations(gameId))
            .build();
    }
}
