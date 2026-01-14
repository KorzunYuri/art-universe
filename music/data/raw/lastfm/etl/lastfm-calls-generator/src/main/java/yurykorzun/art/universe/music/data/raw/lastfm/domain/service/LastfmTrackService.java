package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

import java.util.List;

public interface LastfmTrackService {
    List<LastfmTrack> findTracksForGetInfo();
}
