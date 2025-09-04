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
import yurykorzun.art.universe.music.quiz.dto.GameWithGenerationsDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.service.GameService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Test
    void createGame_shouldReturnGameDto_whenSuccessful() {
        // given
        GameDto expectedDto = GameDto.builder()
            .id(1L)
            .createdAt(Instant.now())
            .build();

        when(gameService.createGame()).thenReturn(expectedDto);

        // when
        GameDto result = gameController.createGame();

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
    void getGameWithGenerations_shouldReturnGameWithGenerationsDto_whenSuccessful() {
        // given
        Long gameId = 1L;
        GameWithGenerationsDto expectedDto = GameWithGenerationsDto.builder()
            .id(gameId)
            .createdAt(Instant.now())
            .generations(List.of(
                GenerationDto.builder().id(1L).gameId(gameId).targetCount(20).build()
            ))
            .build();

        when(gameService.getGameWithGenerations(gameId)).thenReturn(expectedDto);

        // when
        GameWithGenerationsDto result = gameController.getGameWithGenerations(gameId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(gameService).getGameWithGenerations(gameId);
    }
}
