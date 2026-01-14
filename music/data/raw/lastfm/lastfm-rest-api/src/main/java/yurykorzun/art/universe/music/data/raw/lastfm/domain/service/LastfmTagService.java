package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.List;

public interface LastfmTagService {

    LastfmTagResponseDto findById(Long id);

    Page<LastfmTagResponseDto> findAll(TagSearchParams params, Pageable pageable);

    /**
     * Find tags associated with a specific entity with filtering and pagination.
     * @param entityType Type of entity (ARTIST, ALBUM, TRACK)
     * @param entityId ID of the entity
     * @param searchParams Search parameters for filtering
     * @param pageable Pagination and sorting information
     * @return List of tags associated with the entity
     */
    List<EntityTagDto> findAllByEntity(LastfmEntityType entityType, Long entityId, 
                                      EntityTagSearchParams searchParams, Pageable pageable);

    /**
     * Find tags associated with a specific entity.
     * @param entityType Type of entity (ARTIST, ALBUM, TRACK)
     * @param entityId ID of the entity
     * @return List of tags associated with the entity
     */
    default List<EntityTagDto> findAllByEntity(LastfmEntityType entityType, Long entityId) {
        return findAllByEntity(entityType, entityId, new EntityTagSearchParams(null, null), null);
    }

}
