package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;

public interface LastfmTrackService {

    LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
