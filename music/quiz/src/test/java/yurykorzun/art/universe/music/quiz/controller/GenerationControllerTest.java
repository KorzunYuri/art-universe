package yurykorzun.art.universe.music.quiz.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.service.GenerationService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationControllerTest {

    @Mock
    private GenerationService generationService;

    @InjectMocks
    private GenerationController generationController;

    @Test
    void approveGeneration_shouldReturnApprovedGeneration_whenSuccessful() {
        // given
        Long generationId = 1L;
        GenerationDto expectedDto = GenerationDto.builder()
            .id(generationId)
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(true)
            .createdAt(Instant.now())
            .build();

        when(generationService.approveGeneration(generationId)).thenReturn(expectedDto);

        // when
        GenerationDto result = generationController.approveGeneration(generationId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertTrue(result.getApproved());
        verify(generationService).approveGeneration(generationId);
    }

    @Test
    void disapproveGeneration_shouldReturnDisapprovedGeneration_whenSuccessful() {
        // given
        Long generationId = 1L;
        GenerationDto expectedDto = GenerationDto.builder()
            .id(generationId)
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .createdAt(Instant.now())
            .build();

        when(generationService.disapproveGeneration(generationId)).thenReturn(expectedDto);

        // when
        GenerationDto result = generationController.disapproveGeneration(generationId);

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertFalse(result.getApproved());
        verify(generationService).disapproveGeneration(generationId);
    }

    @Test
    void removeTrackFromGeneration_shouldCallService_whenSuccessful() {
        // given
        Long generationId = 1L;
        Long trackId = 100L;

        // when
        generationController.removeTrackFromGeneration(generationId, trackId);

        // then
        verify(generationService).removeTrackFromGeneration(generationId, trackId);
    }
}
