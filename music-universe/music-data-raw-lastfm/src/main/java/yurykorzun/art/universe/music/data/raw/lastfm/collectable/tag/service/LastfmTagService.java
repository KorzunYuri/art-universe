package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;
import java.util.Optional;

public interface LastfmTagService extends EntityService<LastfmTag> {

    Optional<LastfmTag> findById(Long id);

    Page<LastfmTagResponseDto> findAll(TagSearchParams params, Pageable pageable);

    List<LastfmTag> findAllByNameIn(List<String> tagNames);

    List<EntityTagDto> findAllByEntity(LastfmEntityType entityType, Long entityId);

    List<EntityTagDto> findAllByEntityOrderByUsageCount(LastfmEntityType entityType, Long entityId);

    LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
