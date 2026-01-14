package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.List;

/**
 * Custom repository interface for dynamic queries that can't be expressed with Spring Data JPA methods.
 */
public interface LastfmTagRepositoryCustom {
    
    /**
     * Find tags associated with a specific entity with filtering and pagination.
     * Returns tags with additional entity data (entity approval status and usage count).
     * 
     * @param entityType Type of entity (ARTIST, ALBUM, TRACK)
     * @param entityId ID of the entity
     * @param minUsageCount Minimum usage count filter (optional)
     * @param approvalStatuses List of approval statuses to filter by (optional)
     * @param pageable Pagination and sorting information (optional)
     * @return List of entity tag DTOs
     */
    List<EntityTagDto> findTagsByEntityWithFilters(
        LastfmEntityType entityType, 
        Long entityId,
        Integer minUsageCount,
        List<ApprovalStatus> approvalStatuses,
        Pageable pageable
    );
}
