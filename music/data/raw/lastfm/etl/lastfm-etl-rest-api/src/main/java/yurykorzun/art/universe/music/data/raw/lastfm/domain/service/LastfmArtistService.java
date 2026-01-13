package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;

public interface LastfmArtistService {

    LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);

}
