package yurykorzun.art.universe.music.quiz.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithGenerationsDto;

public interface GameService {
    
    GameDto createGame();
    
    Page<GameDto> getAllGames(Pageable pageable);
    
    GameWithGenerationsDto getGameWithGenerations(Long gameId);
}
