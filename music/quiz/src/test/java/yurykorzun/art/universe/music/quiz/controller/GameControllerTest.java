package yurykorzun.art.universe.music.quiz.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithPipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.service.GameService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Test
    void createGame_shouldReturnGameWithPipelineDto_whenSuccessful() {
        // given
        PipelineDto pipelineDto = PipelineDto.builder()
            .id(1L)
            .immutable(false)
            .steps(List.of())
            .build();
            
        GameWithPipelineDto expectedDto = GameWithPipelineDto.builder()
            .id(1L)
            .createdAt(Instant.now())
            .pipeline(pipelineDto)
            .build();

        when(gameService.createGame()).thenReturn(expectedDto);

        // when
        GameWithPipelineDto result = gameController.createGame();

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(gameService).createGame();
    }

    @Test
    void getAllGames_shouldReturnPageOfGameDto_whenSuccessful() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<GameDto> games = List.of(
            GameDto.builder().id(1L).createdAt(Instant.now()).build(),
            GameDto.builder().id(2L).createdAt(Instant.now()).build()
        );
        Page<GameDto> expectedPage = new PageImpl<>(games, pageable, 2);

        when(gameService.getAllGames(pageable)).thenReturn(expectedPage);

        // when
        Page<GameDto> result = gameController.getAllGames(pageable);

        // then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(gameService).getAllGames(pageable);
    }

    @Test
    void getGame_shouldReturnGameWithPipelineDto_whenSuccessful() {
        // given
        Long gameId = 1L;
        GameWithPipelineDto expectedDto = GameWithPipelineDto.builder()
            .id(gameId)
            .createdAt(Instant.now())
            .pipeline(PipelineDto.builder().id(1L).immutable(false).steps(List.of()).build())
            .build();

        when(gameService.getGame(gameId)).thenReturn(expectedDto);

        // when
        GameWithPipelineDto result = gameController.getGame(gameId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(gameService).getGame(gameId);
    }
}
