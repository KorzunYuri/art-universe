package yurykorzun.art.universe.music.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;
import yurykorzun.art.universe.music.quiz.dto.GameDto;
import yurykorzun.art.universe.music.quiz.dto.GameWithPipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.service.GameService;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameService gameService;

    @Test
    void POST_games_shouldReturnGameWithPipelineDto_whenSuccessful() throws Exception {
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

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(gameService).createGame();
    }

    @Test
    void GET_games_shouldReturnPageOfGameDto_whenSuccessful() throws Exception {
        // given
        List<GameDto> games = List.of(
            GameDto.builder().id(1L).createdAt(Instant.now()).build(),
            GameDto.builder().id(2L).createdAt(Instant.now()).build()
        );
        Page<GameDto> expectedPage = new PageImpl<>(games, PageRequest.of(0, 20), 2);

        when(gameService.getAllGames(any())).thenReturn(expectedPage);

        String expectedJson = objectMapper.writeValueAsString(expectedPage);

        // when & then
        mockMvc.perform(get("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(gameService).getAllGames(any());
    }

    @Test
    void GET_game_shouldReturnGameWithPipelineDto_whenSuccessful() throws Exception {
        // given
        Long gameId = 1L;
        GameWithPipelineDto expectedDto = GameWithPipelineDto.builder()
            .id(gameId)
            .createdAt(Instant.now())
            .pipeline(PipelineDto.builder().id(1L).immutable(false).steps(List.of()).build())
            .build();

        when(gameService.getGame(gameId)).thenReturn(expectedDto);

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // when & then
        mockMvc.perform(get("/api/v1/games/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(gameService).getGame(gameId);
    }
}
