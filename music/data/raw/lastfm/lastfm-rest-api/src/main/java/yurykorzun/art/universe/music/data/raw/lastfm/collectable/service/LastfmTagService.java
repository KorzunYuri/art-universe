package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;

public interface LastfmTagService {

    LastfmTagResponseDto findDtoById(Long id);

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

    LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
