package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

import java.util.List;
import java.util.Optional;

public interface LastfmArtistService {
    
    LastfmArtist saveArtist(LastfmArtist artist);

    List<LastfmArtist> saveArtists(List<LastfmArtist> artists);

    Optional<LastfmArtist> findById(long id);

    Optional<LastfmArtist> findByName(String name);

    Page<LastfmArtist> findByName(String name, Pageable pageable);

    List<LastfmArtist> findAllByNames(List<String> names);

    List<LastfmArtist> findAllToGetInfoFor();

    LastfmArtist updateApprovalStatus(Long id, Integer approvalStatusCode);
}
