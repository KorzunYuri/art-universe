package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.TrackSearchParams;

public interface LastfmTrackService {

    LastfmTrackResponseDto findById(Long id);

    Page<LastfmTrackResponseDto> findAll(TrackSearchParams params, Pageable pageable);
    
}
