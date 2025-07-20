package yurykorzun.art.universe.music.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.DimensionDto;
import yurykorzun.art.universe.music.data.master.dto.DimensionSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;

import java.util.List;

public interface DimensionService {

    /**
     * Search dimensions with pagination
     * 
     * @param query Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of dimensions
     */
    Page<DimensionDto> searchDimensions(String query, Pageable pageable);
    
    /**
     * Lookup dimensions by name for dropdown lists
     * 
     * @param name Search term to look for in dimension names (case insensitive, partial match)
     * @param limit Maximum number of results to return (default: 20)
     * @return List of lightweight dimension DTOs with id and name only
     */
    List<LookupResultDTO> lookupDimensions(String name, Integer limit);
    
    /**
     * Lookup dimensions by name for dropdown lists
     * Uses default limit of 20 results.
     * 
     * @param name Search term to look for in dimension names (case insensitive, partial match)
     * @return List of lightweight dimension DTOs with id and name only
     */
    default List<LookupResultDTO> lookupDimensions(String name) {
        return lookupDimensions(name, 20);
    }
    
    /**
     * Save a dimension (create new or update existing)
     * 
     * @param request The dimension save request DTO
     * @return The saved dimension DTO
     */
    DimensionDto saveDimension(DimensionSaveRequestDTO request);
    
    /**
     * Delete a dimension by ID
     * 
     * @param id The dimension ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteDimension(Long id);
}
