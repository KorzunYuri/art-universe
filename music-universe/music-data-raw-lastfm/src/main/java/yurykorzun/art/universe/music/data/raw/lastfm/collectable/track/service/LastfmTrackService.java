package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.List;
import java.util.Optional;

public interface LastfmTrackService extends EntityService<LastfmTrack> {

    LastfmTrack save(LastfmTrack lastfmTrack);

    Page<LastfmTrackResponseDto> findAll(TrackSearchParams params, Pageable pageable);
    
    LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
    
    Optional<LastfmTrack> findById(Long id);
    
    LastfmTrackResponseDto findDtoById(Long id);
    
    /**
     * Find tracks for track.getInfo API call with the following priority:
     * 1. Tracks with missing playCount and listenersCount
     * 2. Prioritize tracks from popular artists (join by artist_id, sort by listenersCount)
     * 
     * @return List of tracks to process with track.getInfo API call
     */
    List<LastfmTrack> findTracksForGetInfo();
}
