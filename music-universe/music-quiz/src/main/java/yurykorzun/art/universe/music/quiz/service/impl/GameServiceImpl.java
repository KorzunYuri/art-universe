package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithGenerationsDto;
import yurykorzun.art.universe.music.quiz.entity.Game;
import yurykorzun.art.universe.music.quiz.repository.GameRepository;
import yurykorzun.art.universe.music.quiz.service.GameService;
import yurykorzun.art.universe.music.quiz.service.GenerationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final GenerationService generationService;

    @Override
    @Transactional
    public GameDto createGame() {
        log.debug("Creating new game");
        
        Game game = Game.builder().build();
        Game savedGame = gameRepository.save(game);
        
        log.debug("Created game with id: {}", savedGame.getId());
        
        return GameDto.builder()
            .id(savedGame.getId())
            .generationId(savedGame.getGenerationId())
            .createdAt(savedGame.getCreatedAt())
            .build();
    }

    @Override
    @Transactional
    public GameDto approveGeneration(Long gameId, Long generationId) {
        log.debug("Approving generation {} for game {}", generationId, gameId);
        
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        game.setGenerationId(generationId);
        Game savedGame = gameRepository.save(game);
        
        log.debug("Approved generation {} for game {}", generationId, gameId);
        
        return GameDto.builder()
            .id(savedGame.getId())
            .generationId(savedGame.getGenerationId())
            .createdAt(savedGame.getCreatedAt())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDto> getAllGames(Pageable pageable) {
        log.debug("Getting all games with pageable: {}", pageable);
        
        return gameRepository.findAll(pageable)
            .map(game -> GameDto.builder()
                .id(game.getId())
                .generationId(game.getGenerationId())
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
            .generationId(game.getGenerationId())
            .createdAt(game.getCreatedAt())
            .generations(generationService.getGenerations(gameId))
            .build();
    }
}
