package yurykorzun.art.universe.music.data.approved.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.entity.Category;
import yurykorzun.art.universe.music.data.approved.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.approved.repository.DimensionRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DimensionRepository dimensionRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void lookupCategories_shouldReturnMatchingCategories() {
        // Given
        String searchTerm = "rock";
        Category category1 = Category.builder().id(1L).name("Rock").build();
        Category category2 = Category.builder().id(2L).name("Alternative Rock").build();
        List<Category> categories = List.of(category1, category2);
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, 20))
            .thenReturn(categories);
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Rock", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Alternative Rock", result.get(1).getName());
        
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, 20);
    }

    @Test
    void lookupCategories_withLimit_shouldReturnLimitedResults() {
        // Given
        String searchTerm = "band";
        int limit = 3;
        
        // Create 5 categories
        List<Category> categories = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> Category.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, limit))
            .thenReturn(categories.subList(0, limit));
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm, limit);
        
        // Then
        assertEquals(limit, result.size());
        for (int i = 0; i < limit; i++) {
            assertEquals((long) (i + 1), result.get(i).getId());
            assertEquals("Band " + (i + 1), result.get(i).getName());
        }
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, limit);
    }

    @Test
    void lookupCategories_withNullLimit_shouldUseDefaultLimit() {
        // Given
        String searchTerm = "band";
        Integer limit = null;
        int defaultLimit = 20;
        
        // Create 30 categories (more than default limit of 20)
        List<Category> categories = IntStream.rangeClosed(1, 30)
            .mapToObj(i -> Category.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, defaultLimit))
            .thenReturn(categories.subList(0, defaultLimit));
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm, limit);
        
        // Then
        assertEquals(defaultLimit, result.size());
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, defaultLimit);
    }

    @Test
    void lookupCategories_withEmptySearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = "";
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupCategories_withNullSearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = null;
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupCategories_withWhitespaceSearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = "   ";
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupCategories_shouldTrimSearchTerm() {
        // Given
        String searchTerm = "  rock  ";
        String trimmedSearchTerm = "rock";
        Category category = Category.builder().id(1L).name("Rock").build();
        List<Category> categories = List.of(category);
        
        when(categoryRepository.findByNameContainingIgnoreCase(trimmedSearchTerm, 20))
            .thenReturn(categories);
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Rock", result.get(0).getName());
        
        verify(categoryRepository).findByNameContainingIgnoreCase(trimmedSearchTerm, 20);
    }

    @Test
    void lookupCategories_withNoMatches_shouldReturnEmptyList() {
        // Given
        String searchTerm = "nonexistent";
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, 20))
            .thenReturn(List.of());
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, 20);
    }
}
