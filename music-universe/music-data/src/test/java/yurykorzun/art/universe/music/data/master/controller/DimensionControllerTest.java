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
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.exception.CustomEntityNotFoundException;
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
    void findDimensions_shouldReturnPageOfDimensions() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        DimensionDto dimension1 = DimensionDto.builder().id(1L).name("Genre").build();
        DimensionDto dimension2 = DimensionDto.builder().id(2L).name("Subgenre").build();
        
        List<DimensionDto> dimensions = Arrays.asList(dimension1, dimension2);
        Page<DimensionDto> expectedPage = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionService.findDimensions(query, pageable)).thenReturn(expectedPage);

        // When
        Page<DimensionDto> result = dimensionController.findDimensions(query, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(dimensionService).findDimensions(query, pageable);
    }

    @Test
    void findDimensions_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(dimensionService.findDimensions(query, pageable)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            dimensionController.findDimensions(query, pageable)
        );
        
        assertSame(expectedException, exception);
        verify(dimensionService).findDimensions(query, pageable);
    }

    @Test
    void lookupDimensions_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "genre";
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(searchTerm)).thenReturn(expectedDimensions);

        // When
        List<LookupResultDTO> result = dimensionController.lookupDimensions(searchTerm, null);

        // Then
        assertEquals(expectedDimensions, result);
        verify(dimensionService).lookupDimensions(searchTerm);
    }

    @Test
    void lookupDimensions_withNullName_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = null;
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(searchTerm)).thenReturn(expectedDimensions);

        // When
        List<LookupResultDTO> result = dimensionController.lookupDimensions(searchTerm, null);

        // Then
        assertEquals(expectedDimensions, result);
        verify(dimensionService).lookupDimensions(searchTerm);
    }

    @Test
    void lookupDimensions_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "genre";
        Integer limit = 5;
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(searchTerm, limit)).thenReturn(expectedDimensions);

        // When
        List<LookupResultDTO> result = dimensionController.lookupDimensions(searchTerm, limit);

        // Then
        assertEquals(expectedDimensions, result);
        verify(dimensionService).lookupDimensions(searchTerm, limit);
    }

    @Test
    void lookupDimensions_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String searchTerm = "genre";
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(dimensionService.lookupDimensions(searchTerm)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            dimensionController.lookupDimensions(searchTerm, null)
        );
        
        assertSame(expectedException, exception);
        verify(dimensionService).lookupDimensions(searchTerm);
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
    void saveDimension_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DimensionSaveRequestDTO request = DimensionSaveRequestDTO.builder()
            .name("New Genre")
            .build();
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(dimensionService.saveDimension(request)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            dimensionController.saveDimension(request)
        );
        
        assertSame(expectedException, exception);
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
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () ->
            dimensionController.deleteDimension(id)
        );
        
        assertEquals("Dimension not found with id: " + id, exception.getMessage());
        verify(dimensionService).deleteDimension(id);
    }

    @Test
    void deleteDimension_whenExceptionThrown_shouldPassThroughException() {
        // Given
        Long id = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(dimensionService.deleteDimension(id)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            dimensionController.deleteDimension(id)
        );
        
        assertSame(expectedException, exception);
        verify(dimensionService).deleteDimension(id);
    }
}
