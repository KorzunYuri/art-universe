package yurykorzun.art.universe.music.data.approved.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;

import java.util.List;

public interface CategoryService {

    /**
     * Search categories with hierarchy information
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of categories with hierarchy information
     */
    Page<CategoryHierarchyProjection> searchCategories(String search, Pageable pageable);
    
    /**
     * Lookup categories by name for dropdown lists
     * 
     * @param name Search term to look for in category names (case insensitive, partial match)
     * @param limit Maximum number of results to return (default: 20)
     * @return List of lightweight category DTOs with id and name only
     */
    List<LookupResultDTO> lookupCategories(String name, Integer limit);
    
    /**
     * Lookup categories by name for dropdown lists
     * Uses default limit of 20 results.
     * 
     * @param name Search term to look for in category names (case insensitive, partial match)
     * @return List of lightweight category DTOs with id and name only
     */
    default List<LookupResultDTO> lookupCategories(String name) {
        return lookupCategories(name, 20);
    }
    
    /**
     * Save a category (create new or update existing)
     * 
     * @param request The category save request DTO
     * @return The saved category with hierarchy information
     */
    CategoryHierarchyProjection saveCategory(CategorySaveRequestDTO request);
    
    /**
     * Delete a category by ID
     * 
     * @param id The category ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteCategory(Long id);
}
