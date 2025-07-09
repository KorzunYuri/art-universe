package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.List;

public interface LastfmTrackService extends EntityService<LastfmTrack> {

    LastfmTrack save(LastfmTrack lastfmTrack);

    Page<LastfmTrackResponseDto> findAll(TrackSearchParams params, Pageable pageable);

    List<LastfmTrack> findAllByUrls(List<String> urls);
    
    LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
}
