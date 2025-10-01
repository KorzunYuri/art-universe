package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;

public interface LastfmArtistService {

    LastfmArtistResponseDto findById(Long id);

    Page<LastfmArtistResponseDto> findAll(ArtistSearchParams params, Pageable pageable);

}
