package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationStepDto;
import yurykorzun.art.universe.music.quiz.entity.Generation;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.GenerationRepository;
import yurykorzun.art.universe.music.quiz.repository.GenerationTrackRepository;
import yurykorzun.art.universe.music.quiz.service.impl.GenerationServiceImpl;

import java.util.List;
import java.util.Map;
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
    void generateTracks_shouldThrowException_whenNoFinalStep() {
        // given
        Long gameId = 1L;
        List<GenerationStepDto> steps = List.of(); // No final step

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.generateTracks(gameId, steps)
        );
        
        assertEquals("At least one final step is required", exception.getMessage());
    }

    @Test
    void generateTracks_shouldThrowException_whenMultipleFinalSteps() {
        // given
        Long gameId = 1L;
        GenerationStepDto finalStep1 = new GenerationStepDto();
        finalStep1.setType(GenerationStepType.FINAL_SELECTION);
        finalStep1.setParams(Map.of("targetCount", 20));
        
        GenerationStepDto finalStep2 = new GenerationStepDto();
        finalStep2.setType(GenerationStepType.FINAL_CATEGORIES_BALANCER);
        finalStep2.setParams(Map.of("targetCount", 20, "categories", List.of()));
        
        List<GenerationStepDto> steps = List.of(finalStep1, finalStep2);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.generateTracks(gameId, steps)
        );
        
        assertEquals("Multiple final steps found. Only one final step is allowed", exception.getMessage());
    }

    @Test
    void generateTracks_shouldThrowException_whenFinalStepMissingTargetCount() {
        // given
        Long gameId = 1L;
        GenerationStepDto finalStep = new GenerationStepDto();
        finalStep.setType(GenerationStepType.FINAL_SELECTION);
        finalStep.setParams(Map.of()); // Missing targetCount
        
        List<GenerationStepDto> steps = List.of(finalStep);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.generateTracks(gameId, steps)
        );
        
        assertEquals("Final step must contain 'targetCount' parameter", exception.getMessage());
    }

    @Test
    void generateTracks_shouldThrowException_whenFinalStepNotLast() {
        // given
        Long gameId = 1L;
        GenerationStepDto finalStep = new GenerationStepDto();
        finalStep.setType(GenerationStepType.FINAL_SELECTION);
        finalStep.setParams(Map.of("targetCount", 20));
        
        GenerationStepDto whitelistStep = new GenerationStepDto();
        whitelistStep.setType(GenerationStepType.WHITELIST_FILTER);
        whitelistStep.setParams(Map.of("categories", List.of()));
        
        List<GenerationStepDto> steps = List.of(finalStep, whitelistStep); // Final step not last

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generationService.generateTracks(gameId, steps)
        );
        
        assertEquals("Final step must be the last step in the configuration", exception.getMessage());
    }

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
