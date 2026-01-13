package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

import java.util.Optional;

public interface LastfmTrackService extends EntityService<LastfmTrack> {
    
    Optional<LastfmTrack> findById(Long id);

}
