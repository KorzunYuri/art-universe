package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTagResponseDto;

public interface LastfmTagService {

    LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
