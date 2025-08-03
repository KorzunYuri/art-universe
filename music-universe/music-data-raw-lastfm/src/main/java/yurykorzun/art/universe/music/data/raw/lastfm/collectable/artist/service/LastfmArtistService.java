package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

import java.util.List;
import java.util.Optional;

public interface LastfmArtistService extends EntityService<LastfmArtist> {
    
    LastfmArtist save(LastfmArtist artist);

    Page<LastfmArtistResponseDto> findAll(ArtistSearchParams params, Pageable pageable);

    Optional<LastfmArtist> findById(Long id);

    Optional<LastfmArtist> findByName(String name);

    List<LastfmArtist> findAllByNames(List<String> names);

    List<LastfmArtist> findArtistsForGetInfo();

    LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
}
