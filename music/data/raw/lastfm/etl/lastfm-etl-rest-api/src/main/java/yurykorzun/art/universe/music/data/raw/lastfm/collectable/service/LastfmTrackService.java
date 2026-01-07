package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;

public interface LastfmTrackService {

    LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
