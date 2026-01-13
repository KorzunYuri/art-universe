package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;

public interface LastfmArtistService {

    LastfmArtistResponseDto findById(Long id);

    Page<LastfmArtistResponseDto> findAll(ArtistSearchParams params, Pageable pageable);

}
