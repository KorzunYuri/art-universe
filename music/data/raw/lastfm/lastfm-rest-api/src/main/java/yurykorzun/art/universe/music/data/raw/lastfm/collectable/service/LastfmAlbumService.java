package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;

public interface LastfmAlbumService {

    LastfmAlbumResponseDto findById(Long id);
    
    Page<LastfmAlbumResponseDto> findAll(AlbumSearchParams params, Pageable pageable);

}
