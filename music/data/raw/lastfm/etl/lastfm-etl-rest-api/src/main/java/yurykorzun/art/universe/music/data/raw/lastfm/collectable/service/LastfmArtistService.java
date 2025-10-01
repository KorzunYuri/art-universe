package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;

public interface LastfmArtistService {

    LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
