package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;

public interface LastfmTagService {

    LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
