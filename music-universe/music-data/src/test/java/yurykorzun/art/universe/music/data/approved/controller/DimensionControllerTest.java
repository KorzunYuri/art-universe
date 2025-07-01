package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.DimensionDto;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.service.DimensionService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DimensionControllerTest {

    @Mock
    private DimensionService dimensionService;

    @InjectMocks
    private DimensionController dimensionController;

    @Test
    void searchDimensions_shouldReturnSuccessResponse() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        DimensionDto dimension1 = DimensionDto.builder().id(1L).name("Genre").build();
        DimensionDto dimension2 = DimensionDto.builder().id(2L).name("Subgenre").build();
        
        List<DimensionDto> dimensions = Arrays.asList(dimension1, dimension2);
        Page<DimensionDto> page = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionService.searchDimensions(query, pageable)).thenReturn(page);
        ResponseEntity<ResponseWrapper<Page<DimensionDto>>> expectedResponse = 
            ResponseWrapper.success(page);

        // When
        ResponseEntity<ResponseWrapper<Page<DimensionDto>>> actualResponse = 
            dimensionController.searchDimensions(query, pageable);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(dimensionService).searchDimensions(query, pageable);
    }

    @Test
    void searchDimensions_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Test error";
        
        when(dimensionService.searchDimensions(query, pageable)).thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<Page<DimensionDto>>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to search dimensions: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Page<DimensionDto>>> actualResponse = 
            dimensionController.searchDimensions(query, pageable);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(dimensionService).searchDimensions(query, pageable);
    }

    @Test
    void lookupDimensions_shouldReturnSuccessResponse() {
        // Given
        String name = "genre";
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(name)).thenReturn(expectedDimensions);
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse = 
            ResponseWrapper.success(expectedDimensions);

        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse = 
            dimensionController.lookupDimensions(name, null);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(dimensionService).lookupDimensions(name);
    }

    @Test
    void lookupDimensions_withLimit_shouldReturnSuccessResponse() {
        // Given
        String name = "genre";
        Integer limit = 5;
        LookupResultDTO dimension1 = new LookupResultDTO(1L, "Genre");
        LookupResultDTO dimension2 = new LookupResultDTO(2L, "Subgenre");
        List<LookupResultDTO> expectedDimensions = Arrays.asList(dimension1, dimension2);
        
        when(dimensionService.lookupDimensions(name, limit)).thenReturn(expectedDimensions);
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse = 
            ResponseWrapper.success(expectedDimensions);

        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse = 
            dimensionController.lookupDimensions(name, limit);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(dimensionService).lookupDimensions(name, limit);
    }

    @Test
    void lookupDimensions_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        String name = "genre";
        String errorMessage = "Test error";
        
        when(dimensionService.lookupDimensions(name)).thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to lookup dimensions: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse = 
            dimensionController.lookupDimensions(name, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(dimensionService).lookupDimensions(name);
    }
}
