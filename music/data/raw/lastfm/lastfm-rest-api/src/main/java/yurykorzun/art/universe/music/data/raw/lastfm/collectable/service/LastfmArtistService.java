package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;

public interface LastfmArtistService {

    Page<LastfmArtistResponseDto> findAll(ArtistSearchParams params, Pageable pageable);

    LastfmArtistResponseDto findDtoById(Long id);

    LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
}
