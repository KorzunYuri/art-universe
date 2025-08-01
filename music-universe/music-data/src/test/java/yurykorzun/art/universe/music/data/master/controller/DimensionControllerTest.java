package yurykorzun.art.universe.music.data.master.controller;

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
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.DimensionService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DimensionControllerTest {

    @Mock
    private DimensionService dimensionService;

    @InjectMocks
    private DimensionController dimensionController;

    @Test
    void searchDimensions_shouldReturnPageOfDimensions() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        DimensionDto dimension1 = DimensionDto.builder().id(1L).name("Genre").build();
        DimensionDto dimension2 = DimensionDto.builder().id(2L).name("Subgenre").build();
        
        List<DimensionDto> dimensions = Arrays.asList(dimension1, dimension2);
        Page<DimensionDto> expectedPage = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionService.searchDimensions(query, pageable)).thenReturn(expectedPage);

        // When
        Page<DimensionDto> result = dimensionController.searchDimensions(query, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(dimensionService).searchDimensions(query, pageable);
    }

    @Test
    void searchDimensions_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Test error";
        
        when(dimensionService.searchDimensions(query, pageable)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            dimensionController.searchDimensions(query, pageable)
        );
        
        assertEquals("Failed to search dimensions: " + errorMessage, exception.getMessage());
        verify(dimensionService).searchDimensions(query, pageable);
    }

    @Test
    void lookupDimensions_shouldReturnListOfLookupResults() {
        // Given
        String name = "genre";
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(name)).thenReturn(expectedDimensions);

        // When
        List<LookupResultDTO> result = dimensionController.lookupDimensions(name, null);

        // Then
        assertEquals(expectedDimensions, result);
        verify(dimensionService).lookupDimensions(name);
    }

    @Test
    void lookupDimensions_withNullName_shouldReturnListOfLookupResults() {
        // Given
        String name = null;
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(name)).thenReturn(expectedDimensions);

        // When
        List<LookupResultDTO> result = dimensionController.lookupDimensions(name, null);

        // Then
        assertEquals(expectedDimensions, result);
        verify(dimensionService).lookupDimensions(name);
    }

    @Test
    void lookupDimensions_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String name = "genre";
        Integer limit = 5;
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(name, limit)).thenReturn(expectedDimensions);

        // When
        List<LookupResultDTO> result = dimensionController.lookupDimensions(name, limit);

        // Then
        assertEquals(expectedDimensions, result);
        verify(dimensionService).lookupDimensions(name, limit);
    }

    @Test
    void lookupDimensions_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String name = "genre";
        String errorMessage = "Test error";
        
        when(dimensionService.lookupDimensions(name)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            dimensionController.lookupDimensions(name, null)
        );
        
        assertEquals("Failed to lookup dimensions: " + errorMessage, exception.getMessage());
        verify(dimensionService).lookupDimensions(name);
    }

    @Test
    void saveDimension_shouldReturnDimensionDto() {
        // Given
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .name("New Genre")
            .build();
        
        DimensionDto savedDimension = DimensionDto.builder()
            .id(1L)
            .name("New Genre")
            .build();
        
        when(dimensionService.saveDimension(request)).thenReturn(savedDimension);

        // When
        DimensionDto result = dimensionController.saveDimension(request);

        // Then
        assertEquals(savedDimension, result);
        verify(dimensionService).saveDimension(request);
    }

    @Test
    void saveDimension_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .name("New Genre")
            .build();
        String errorMessage = "Test error";
        
        when(dimensionService.saveDimension(request)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            dimensionController.saveDimension(request)
        );
        
        assertEquals("Failed to save dimension: " + errorMessage, exception.getMessage());
        verify(dimensionService).saveDimension(request);
    }

    @Test
    void deleteDimension_whenFound_shouldReturnTrue() {
        // Given
        Long id = 1L;
        
        when(dimensionService.deleteDimension(id)).thenReturn(true);

        // When
        boolean result = dimensionController.deleteDimension(id);

        // Then
        assertTrue(result);
        verify(dimensionService).deleteDimension(id);
    }

    @Test
    void deleteDimension_whenNotFound_shouldThrowEntityNotFoundException() {
        // Given
        Long id = 1L;
        
        when(dimensionService.deleteDimension(id)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            dimensionController.deleteDimension(id)
        );
        
        assertEquals("Dimension not found with id: " + id, exception.getMessage());
        verify(dimensionService).deleteDimension(id);
    }

    @Test
    void deleteDimension_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        Long id = 1L;
        String errorMessage = "Test error";
        
        when(dimensionService.deleteDimension(id)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            dimensionController.deleteDimension(id)
        );
        
        assertEquals("Failed to delete dimension: " + errorMessage, exception.getMessage());
        verify(dimensionService).deleteDimension(id);
    }
}
