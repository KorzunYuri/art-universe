package yurykorzun.art.universe.music.data.approved.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;

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
