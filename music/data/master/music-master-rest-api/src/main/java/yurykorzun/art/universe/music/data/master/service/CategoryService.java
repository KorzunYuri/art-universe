package yurykorzun.art.universe.music.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.domain.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;

import java.util.List;

public interface CategoryService {

    /**
     * Search categories with hierarchy information
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of categories with hierarchy information
     */
    Page<CategoryDto> findCategories(String search, Pageable pageable);

    /**
     * Find categories with their parent information
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of categories with parent information
     */
    Page<CategoryWithParentsDto> findCategoriesWithParents(String search, Pageable pageable);

    /**
     * Returns a single category with a provided id
     */
    CategoryDto getCategory(Long id);

    /**
     * Returns a single category with parent information by id
     */
    CategoryWithParentsDto getCategoryWithParents(Long id);

    /**
     * Lookup categories by name for dropdown lists
     * 
     * @param request The lookup request containing search term and optional limit
     * @return List of lightweight category DTOs with id and name only
     */
    List<LookupResultDTO> lookupCategories(LookupRequestDTO request);
    
    /**
     * Batch lookup categories by multiple search requests
     * 
     * @param request The batch lookup request containing search requests and optional limit
     * @return Map of search terms to lists of matching categories
     */
    BatchLookupResponseDTO batchLookupCategories(BaseBatchLookupRequestDTO request);
    
    /**
     * Save a category (create new or update existing)
     * 
     * @param request The category save request DTO
     * @return The saved category with hierarchy information
     */
    CategoryDto saveCategory(CategorySaveRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus);
    
    /**
     * Save a category with parent relationships (create new or update existing)
     * 
     * @param request The category save request DTO with parent information
     * @return The saved category with parent information
     */
    CategoryWithParentsDto saveCategoryWithParents(CategorySaveWithParentsRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus);
    
    /**
     * Delete a category by ID
     * 
     * @param id The category ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteCategory(Long id);
    
    /**
     * Find bound categories for a data source and external IDs
     * 
     * @param dataSource The external data source
     * @param externalIds List of external category IDs
     * @return List of bound category information
     */
    List<BoundEntityProjection> findBoundCategories(DataSource dataSource, List<Long> externalIds);
    
    /**
     * Find a single bound category by data source and external ID
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the category in the external system
     * @return The bound category information, or null if not found
     */
    BoundEntityProjection findBoundCategory(DataSource dataSource, Long externalId);
    
    /**
     * Bind an external category to an existing category in the system
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the category in the external system
     * @param request The binding request containing category ID
     * @return The created binding information
     */
    BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, EntityBindToExistingRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus);
    
    /**
     * Create a new category and bind an external category to it
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the category in the external system
     * @param request The binding request containing category information
     * @return The created binding information
     */
    BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, EntityCreateAndBindRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus);
    
    /**
     * Unbind an external category from the system
     * 
     * @param dataSource The external data source
     * @param externalId The ID of the category in the external system
     * @return true if the unbinding was successful, false otherwise
     */
    boolean unbindCategory(DataSource dataSource, Long externalId);

    /**
     * Get complete category DAG for UI visualization
     * 
     * @return Category DAG with nodes and edges
     */
    CategoryDagDTO getCategoryDag();

    /**
     * Create a relation between two categories
     * 
     * @param relation The relation DTO with source and target category IDs
     * @throws CycleRelationException if the relation would create a cycle
     * @throws DataUpdateException if the relation already exists
     */
    void createCategoryRelation(CategoryRelationDTO relation);

    /**
     * Delete a relation between two categories
     * 
     * @param relation The relation DTO with source and target category IDs
     * @throws EntityNotFoundException if the relation does not exist
     */
    void deleteCategoryRelation(CategoryRelationDTO relation);

}
