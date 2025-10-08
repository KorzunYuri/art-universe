package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;

public interface LastfmAlbumService {

    LastfmAlbumResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
