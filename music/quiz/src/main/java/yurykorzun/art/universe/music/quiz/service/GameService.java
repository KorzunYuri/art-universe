package yurykorzun.art.universe.music.quiz.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithPipelineDto;

public interface GameService {
    
    GameWithPipelineDto createGame();

    GameWithPipelineDto getGame(Long gameId);

    Page<GameDto> getAllGames(Pageable pageable);
}
