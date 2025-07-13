package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;
import java.util.Optional;

public interface LastfmTagService extends EntityService<LastfmTag> {

    Optional<LastfmTag> findById(Long id);

    Page<LastfmTagResponseDto> findAll(TagSearchParams params, Pageable pageable);

    List<LastfmTag> findAllByNameIn(List<String> tagNames);

    /**
     * Find tags associated with a specific entity.
     * @param entityType Type of entity (ARTIST, ALBUM, TRACK)
     * @param entityId ID of the entity
     * @return List of tags associated with the entity
     */
    default List<EntityTagDto> findAllByEntity(LastfmEntityType entityType, Long entityId) {
        return findAllByEntity(entityType, entityId, new EntityTagSearchParams(null, null), null);
    }

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

    LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
