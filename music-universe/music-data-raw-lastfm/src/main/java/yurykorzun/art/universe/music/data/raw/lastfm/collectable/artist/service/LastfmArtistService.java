package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

import java.util.List;
import java.util.Optional;

public interface LastfmArtistService {
    
    LastfmArtist saveArtist(LastfmArtist artist);

    List<LastfmArtist> saveArtists(List<LastfmArtist> artists);

    Optional<LastfmArtist> findById(Long id);

    Optional<LastfmArtist> findByName(String name);

    Page<LastfmArtistResponseDto> findArtists(ArtistSearchParams params, Pageable pageable);

    List<LastfmArtist> findAllByNames(List<String> names);

    List<LastfmArtist> findAllToGetInfoFor();

    LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
}
