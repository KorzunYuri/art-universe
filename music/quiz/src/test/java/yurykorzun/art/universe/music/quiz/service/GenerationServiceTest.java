package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.entity.Generation;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.repository.GenerationRepository;
import yurykorzun.art.universe.music.quiz.repository.GenerationTrackRepository;
import yurykorzun.art.universe.music.quiz.service.impl.GenerationServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationServiceTest {

    @Mock
    private GenerationRepository generationRepository;

    @Mock
    private GenerationTrackRepository generationTrackRepository;

    @InjectMocks
    private GenerationServiceImpl generationService;

    @Test
    void approveGeneration_shouldReturnApprovedGeneration_whenGenerationExists() {
        // given
        Long generationId = 1L;
        Generation generation = Generation.builder()
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .build();
        
        Generation approvedGeneration = Generation.builder()
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(true)
            .build();
        
        when(generationRepository.findById(generationId)).thenReturn(Optional.of(generation));
        when(generationRepository.save(any(Generation.class))).thenReturn(approvedGeneration);

        // when
        GenerationDto result = generationService.approveGeneration(generationId);

        // then
        assertNotNull(result);
        assertTrue(result.getApproved());
        verify(generationRepository).findById(generationId);
        verify(generationRepository).save(any(Generation.class));
    }

    @Test
    void disapproveGeneration_shouldReturnDisapprovedGeneration_whenGenerationExists() {
        // given
        Long generationId = 1L;
        Generation generation = Generation.builder()
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(true)
            .build();
        
        Generation disapprovedGeneration = Generation.builder()
            .gameId(1L)
            .targetCount(20)
            .status(GenerationStatus.COMPLETED)
            .approved(false)
            .build();
        
        when(generationRepository.findById(generationId)).thenReturn(Optional.of(generation));
        when(generationRepository.save(any(Generation.class))).thenReturn(disapprovedGeneration);

        // when
        GenerationDto result = generationService.disapproveGeneration(generationId);

        // then
        assertNotNull(result);
        assertFalse(result.getApproved());
        verify(generationRepository).findById(generationId);
        verify(generationRepository).save(any(Generation.class));
    }

    @Test
    void approveGeneration_shouldThrowException_whenGenerationNotFound() {
        // given
        Long generationId = 999L;
        when(generationRepository.findById(generationId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.approveGeneration(generationId)
        );
        
        assertEquals("Generation not found: 999", exception.getMessage());
        verify(generationRepository).findById(generationId);
        verify(generationRepository, never()).save(any());
    }

    @Test
    void removeTrackFromGeneration_shouldRemoveTrack_whenGenerationExists() {
        // given
        Long generationId = 1L;
        Long trackId = 100L;
        
        when(generationRepository.existsById(generationId)).thenReturn(true);

        // when
        generationService.removeTrackFromGeneration(generationId, trackId);

        // then
        verify(generationRepository).existsById(generationId);
        verify(generationTrackRepository).deleteByGenerationIdAndTrackId(generationId, trackId);
    }

    @Test
    void removeTrackFromGeneration_shouldThrowException_whenGenerationNotFound() {
        // given
        Long generationId = 999L;
        Long trackId = 100L;
        
        when(generationRepository.existsById(generationId)).thenReturn(false);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.removeTrackFromGeneration(generationId, trackId)
        );
        
        assertEquals("Generation not found: 999", exception.getMessage());
        verify(generationRepository).existsById(generationId);
        verify(generationTrackRepository, never()).deleteByGenerationIdAndTrackId(any(), any());
    }
}
