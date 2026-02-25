package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumTrackResponseDto;

import java.util.List;

public interface LastfmAlbumService {

    LastfmAlbumResponseDto findById(Long id);

    Page<LastfmAlbumResponseDto> findAll(AlbumSearchParams params, Pageable pageable);

    List<LastfmAlbumTrackResponseDto> findAlbumTracks(Long albumId);

}
