package yurykorzun.art.universe.music.quiz.service;

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
import yurykorzun.art.universe.music.quiz.dto.GameWithPipelineDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.entity.Game;
import yurykorzun.art.universe.music.quiz.repository.GameRepository;
import yurykorzun.art.universe.music.quiz.service.impl.GameServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GenerationService generationService;
    
    @Mock
    private PipelineService pipelineService;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void createGame_shouldReturnGameWithPipelineDto_whenSuccessful() {
        // given
        Game savedGame = Game.builder().pipelineId(1L).build();
        
        // Use reflection to set id and timestamps
        try {
            var idField = Game.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedGame, 1L);
            
            var createdAtField = Game.class.getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            Instant now = Instant.now();
            createdAtField.set(savedGame, now);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PipelineDto pipelineDto = PipelineDto.builder()
            .id(1L)
            .immutable(false)
            .steps(List.of())
            .build();

        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);
        when(pipelineService.createBasicPipeline()).thenReturn(pipelineDto);

        // when
        GameWithPipelineDto result = gameService.createGame();

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getPipeline());
        assertEquals(1L, result.getPipeline().getId());
        
        verify(gameRepository).save(any(Game.class));
        verify(pipelineService).createBasicPipeline();
    }

    @Test
    void getAllGames_shouldReturnPageOfGameDto_whenSuccessful() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        Game game1 = Game.builder().pipelineId(1L).build();
        Game game2 = Game.builder().pipelineId(2L).build();
        
        // Use reflection to set ids and timestamps
        try {
            var idField = Game.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(game1, 1L);
            idField.set(game2, 2L);
            
            var createdAtField = Game.class.getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            Instant now = Instant.now();
            createdAtField.set(game1, now);
            createdAtField.set(game2, now);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Page<Game> gamePage = new PageImpl<>(List.of(game1, game2), pageable, 2);
        when(gameRepository.findAll(pageable)).thenReturn(gamePage);

        // when
        Page<GameDto> result = gameService.getAllGames(pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        
        GameDto dto1 = result.getContent().get(0);
        assertEquals(1L, dto1.getId());
        assertNotNull(dto1.getCreatedAt());
        
        GameDto dto2 = result.getContent().get(1);
        assertEquals(2L, dto2.getId());
        assertNotNull(dto2.getCreatedAt());
        
        verify(gameRepository).findAll(pageable);
    }

    @Test
    void getGameWithGenerations_shouldReturnGameWithGenerations_whenGameExists() {
        // given
        Long gameId = 1L;
        
        Game existingGame = Game.builder().pipelineId(1L).build();
        
        // Use reflection to set id and timestamps
        try {
            var idField = Game.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingGame, gameId);
            
            var createdAtField = Game.class.getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(existingGame, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<GenerationDto> generations = List.of(
            GenerationDto.builder().id(1L).gameId(gameId).targetCount(20).build(),
            GenerationDto.builder().id(2L).gameId(gameId).targetCount(15).build()
        );

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(existingGame));
        when(generationService.getGenerations(gameId)).thenReturn(generations);

        // when
        GameWithGenerationsDto result = gameService.getGameWithGenerations(gameId);

        // then
        assertNotNull(result);
        assertEquals(gameId, result.getId());
        assertNotNull(result.getCreatedAt());
        assertEquals(2, result.getGenerations().size());
        verify(gameRepository).findById(gameId);
        verify(generationService).getGenerations(gameId);
    }

    @Test
    void getGameWithGenerations_shouldThrowException_whenGameNotFound() {
        // given
        Long gameId = 999L;

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> gameService.getGameWithGenerations(gameId)
        );
        
        assertEquals("Game not found: 999", exception.getMessage());
        verify(gameRepository).findById(gameId);
        verify(generationService, never()).getGenerations(any());
    }
}
