package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;

public interface LastfmTagService {
    List<LastfmTag> findAllByNameIn(List<String> tagNames);
    List<LastfmTag> saveTags(List<LastfmTag> tags);
    Page<LastfmTagResponseDto> findTags(TagSearchParams params, Pageable pageable);
    LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
}
