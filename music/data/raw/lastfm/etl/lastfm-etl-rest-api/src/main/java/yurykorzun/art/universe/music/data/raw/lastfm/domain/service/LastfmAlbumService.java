package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;

public interface LastfmAlbumService {

    LastfmAlbumResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
