package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.List;
import java.util.Optional;

public interface LastfmTrackService {

    LastfmTrack saveTrack(LastfmTrack lastfmTrack);

    List<LastfmTrack> saveTracks(List<LastfmTrack> lastfmTracks);

    List<LastfmTrack> findAllByUrls(List<String> urls);
    
    Page<LastfmTrackResponseDto> findTracks(TrackSearchParams params, Pageable pageable);
    
    LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
}
