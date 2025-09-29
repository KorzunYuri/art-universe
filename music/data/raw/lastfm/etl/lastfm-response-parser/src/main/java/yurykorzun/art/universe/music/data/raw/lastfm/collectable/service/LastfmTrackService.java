package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

import java.util.Optional;

public interface LastfmTrackService extends EntityService<LastfmTrack> {
    
    Optional<LastfmTrack> findById(Long id);

}
