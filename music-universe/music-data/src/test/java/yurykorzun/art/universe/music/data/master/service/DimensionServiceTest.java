package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.DimensionDto;
import yurykorzun.art.universe.music.data.master.dto.DimensionSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.Dimension;
import yurykorzun.art.universe.music.data.master.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.DimensionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DimensionServiceTest {

    @Mock
    private DimensionRepository dimensionRepository;

    @InjectMocks
    private DimensionServiceImpl dimensionService;

    @Test
    void findDimensions_shouldReturnPageOfDimensionDtos() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        Dimension dimension1 = Dimension.builder().id(1L).name("Genre").build();
        Dimension dimension2 = Dimension.builder().id(2L).name("Subgenre").build();
        List<Dimension> dimensions = List.of(dimension1, dimension2);
        Page<Dimension> dimensionPage = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionRepository.searchDimensions(query, pageable)).thenReturn(dimensionPage);

        // When
        Page<DimensionDto> result = dimensionService.findDimensions(query, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("Genre", result.getContent().get(0).getName());
        assertEquals(2L, result.getContent().get(1).getId());
        assertEquals("Subgenre", result.getContent().get(1).getName());
        assertEquals(2, result.getTotalElements());
        
        verify(dimensionRepository).searchDimensions(query, pageable);
    }

    @Test
    void findDimensions_withNullQuery_shouldReturnAllDimensions() {
        // Given
        String query = null;
        Pageable pageable = PageRequest.of(0, 10);
        
        Dimension dimension = Dimension.builder().id(1L).name("Genre").build();
        List<Dimension> dimensions = List.of(dimension);
        Page<Dimension> dimensionPage = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionRepository.searchDimensions(query, pageable)).thenReturn(dimensionPage);

        // When
        Page<DimensionDto> result = dimensionService.findDimensions(query, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Genre", result.getContent().get(0).getName());
        
        verify(dimensionRepository).searchDimensions(query, pageable);
    }

    @Test
    void saveDimension_whenCreatingNew_shouldCreateDimension() {
        // Given
        String dimensionName = "New Genre";
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .name(dimensionName)
            .build();
        
        Dimension savedDimension = Dimension.builder()
            .id(1L)
            .name(dimensionName)
            .build();
        
        when(dimensionRepository.findByNameIgnoreCase(dimensionName)).thenReturn(Optional.empty());
        when(dimensionRepository.save(any(Dimension.class))).thenReturn(savedDimension);
        
        // When
        DimensionDto result = dimensionService.saveDimension(request);
        
        // Then
        assertEquals(1L, result.getId());
        assertEquals(dimensionName, result.getName());
        
        verify(dimensionRepository).findByNameIgnoreCase(dimensionName);
        verify(dimensionRepository).save(any(Dimension.class));
    }

    @Test
    void saveDimension_whenCreatingNewWithExistingName_shouldThrowException() {
        // Given
        String dimensionName = "Existing Genre";
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .name(dimensionName)
            .build();
        
        Dimension existingDimension = Dimension.builder()
            .id(1L)
            .name(dimensionName)
            .build();
        
        when(dimensionRepository.findByNameIgnoreCase(dimensionName)).thenReturn(Optional.of(existingDimension));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> dimensionService.saveDimension(request));
        
        assertEquals("Dimension with name 'Existing Genre' already exists", exception.getMessage());
        
        verify(dimensionRepository).findByNameIgnoreCase(dimensionName);
        verify(dimensionRepository, never()).save(any());
    }

    @Test
    void saveDimension_whenUpdatingExisting_shouldUpdateDimension() {
        // Given
        Long dimensionId = 1L;
        String newName = "Updated Genre";
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .id(dimensionId)
            .name(newName)
            .build();
        
        Dimension existingDimension = Dimension.builder()
            .id(dimensionId)
            .name("Old Genre")
            .build();
        
        Dimension updatedDimension = Dimension.builder()
            .id(dimensionId)
            .name(newName)
            .build();
        
        when(dimensionRepository.findById(dimensionId)).thenReturn(Optional.of(existingDimension));
        when(dimensionRepository.save(existingDimension)).thenReturn(updatedDimension);
        
        // When
        DimensionDto result = dimensionService.saveDimension(request);
        
        // Then
        assertEquals(dimensionId, result.getId());
        assertEquals(newName, result.getName());
        assertEquals(newName, existingDimension.getName()); // Verify the entity was updated
        
        verify(dimensionRepository).findById(dimensionId);
        verify(dimensionRepository).save(existingDimension);
        verify(dimensionRepository, never()).findByNameIgnoreCase(any());
    }

    @Test
    void saveDimension_whenUpdatingNonExistent_shouldThrowException() {
        // Given
        Long dimensionId = 999L;
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .id(dimensionId)
            .name("Some Name")
            .build();
        
        when(dimensionRepository.findById(dimensionId)).thenReturn(Optional.empty());
        
        // When & Then
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class,
            () -> dimensionService.saveDimension(request));
        
        assertEquals("Dimension not found with id: 999", exception.getMessage());
        
        verify(dimensionRepository).findById(dimensionId);
        verify(dimensionRepository, never()).save(any());
    }

    @Test
    void deleteDimension_whenDimensionExists_shouldDeleteAndReturnTrue() {
        // Given
        Long dimensionId = 1L;
        
        when(dimensionRepository.existsById(dimensionId)).thenReturn(true);
        doNothing().when(dimensionRepository).deleteById(dimensionId);
        
        // When
        boolean result = dimensionService.deleteDimension(dimensionId);
        
        // Then
        assertTrue(result);
        
        verify(dimensionRepository).existsById(dimensionId);
        verify(dimensionRepository).deleteById(dimensionId);
    }

    @Test
    void deleteDimension_whenDimensionDoesNotExist_shouldReturnFalse() {
        // Given
        Long dimensionId = 999L;
        
        when(dimensionRepository.existsById(dimensionId)).thenReturn(false);
        
        // When
        boolean result = dimensionService.deleteDimension(dimensionId);
        
        // Then
        assertFalse(result);
        
        verify(dimensionRepository).existsById(dimensionId);
        verify(dimensionRepository, never()).deleteById(any());
    }
}
