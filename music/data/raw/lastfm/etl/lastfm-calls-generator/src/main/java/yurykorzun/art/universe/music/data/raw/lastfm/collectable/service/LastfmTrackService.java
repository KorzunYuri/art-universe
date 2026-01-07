package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

import java.util.List;

public interface LastfmTrackService {
    List<LastfmTrack> findTracksForGetInfo();
}
