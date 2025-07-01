package yurykorzun.art.universe.music.data.approved.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.approved.dto.DimensionDto;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.entity.Dimension;
import yurykorzun.art.universe.music.data.approved.repository.DimensionRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DimensionServiceTest {

    @Mock
    private DimensionRepository dimensionRepository;

    @InjectMocks
    private DimensionServiceImpl dimensionService;

    @Test
    void searchDimensions_shouldReturnPageOfDimensionDtos() {
        // Given
        String query = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        Dimension dimension1 = Dimension.builder().id(1L).name("Genre").build();
        Dimension dimension2 = Dimension.builder().id(2L).name("Subgenre").build();
        List<Dimension> dimensions = List.of(dimension1, dimension2);
        Page<Dimension> dimensionPage = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionRepository.searchDimensions(query, pageable)).thenReturn(dimensionPage);

        // When
        Page<DimensionDto> result = dimensionService.searchDimensions(query, pageable);

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
    void searchDimensions_withNullQuery_shouldReturnAllDimensions() {
        // Given
        String query = null;
        Pageable pageable = PageRequest.of(0, 10);
        
        Dimension dimension = Dimension.builder().id(1L).name("Genre").build();
        List<Dimension> dimensions = List.of(dimension);
        Page<Dimension> dimensionPage = new PageImpl<>(dimensions, pageable, dimensions.size());
        
        when(dimensionRepository.searchDimensions(query, pageable)).thenReturn(dimensionPage);

        // When
        Page<DimensionDto> result = dimensionService.searchDimensions(query, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Genre", result.getContent().get(0).getName());
        
        verify(dimensionRepository).searchDimensions(query, pageable);
    }

    @Test
    void lookupDimensions_shouldReturnMatchingDimensions() {
        // Given
        String searchTerm = "genre";
        Integer limit = 5;
        Dimension dimension1 = Dimension.builder().id(1L).name("Genre").build();
        Dimension dimension2 = Dimension.builder().id(2L).name("Subgenre").build();
        List<Dimension> dimensions = List.of(dimension1, dimension2);
        
        when(dimensionRepository.findByNameContainingIgnoreCase(searchTerm, limit))
            .thenReturn(dimensions);

        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm, limit);

        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Genre", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Subgenre", result.get(1).getName());
        
        verify(dimensionRepository).findByNameContainingIgnoreCase(searchTerm, limit);
    }

    @Test
    void lookupDimensions_withDefaultLimit_shouldUseDefaultLimit() {
        // Given
        String searchTerm = "genre";
        int defaultLimit = 20;
        
        // Create 30 dimensions (more than default limit of 20)
        List<Dimension> dimensions = IntStream.rangeClosed(1, 30)
            .mapToObj(i -> Dimension.builder().id((long) i).name("Genre " + i).build())
            .collect(Collectors.toList());
        
        when(dimensionRepository.findByNameContainingIgnoreCase(searchTerm, defaultLimit))
            .thenReturn(dimensions.subList(0, defaultLimit));
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm);
        
        // Then
        assertEquals(defaultLimit, result.size());
        verify(dimensionRepository).findByNameContainingIgnoreCase(searchTerm, defaultLimit);
    }

    @Test
    void lookupDimensions_withNullLimit_shouldUseDefaultLimit() {
        // Given
        String searchTerm = "genre";
        Integer limit = null;
        int defaultLimit = 20;
        
        // Create 30 dimensions (more than default limit of 20)
        List<Dimension> dimensions = IntStream.rangeClosed(1, 30)
            .mapToObj(i -> Dimension.builder().id((long) i).name("Genre " + i).build())
            .collect(Collectors.toList());
        
        when(dimensionRepository.findByNameContainingIgnoreCase(searchTerm, defaultLimit))
            .thenReturn(dimensions.subList(0, defaultLimit));
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm, limit);
        
        // Then
        assertEquals(defaultLimit, result.size());
        verify(dimensionRepository).findByNameContainingIgnoreCase(searchTerm, defaultLimit);
    }

    @Test
    void lookupDimensions_withEmptySearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = "";
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(dimensionRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupDimensions_withNullSearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = null;
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(dimensionRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupDimensions_withWhitespaceSearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = "   ";
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(dimensionRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupDimensions_shouldTrimSearchTerm() {
        // Given
        String searchTerm = "  genre  ";
        String trimmedSearchTerm = "genre";
        Dimension dimension = Dimension.builder().id(1L).name("Genre").build();
        List<Dimension> dimensions = List.of(dimension);
        
        when(dimensionRepository.findByNameContainingIgnoreCase(trimmedSearchTerm, 20))
            .thenReturn(dimensions);
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Genre", result.get(0).getName());
        
        verify(dimensionRepository).findByNameContainingIgnoreCase(trimmedSearchTerm, 20);
    }

    @Test
    void lookupDimensions_withNoMatches_shouldReturnEmptyList() {
        // Given
        String searchTerm = "nonexistent";
        
        when(dimensionRepository.findByNameContainingIgnoreCase(searchTerm, 20))
            .thenReturn(List.of());
        
        // When
        List<LookupResultDTO> result = dimensionService.lookupDimensions(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(dimensionRepository).findByNameContainingIgnoreCase(searchTerm, 20);
    }
}
