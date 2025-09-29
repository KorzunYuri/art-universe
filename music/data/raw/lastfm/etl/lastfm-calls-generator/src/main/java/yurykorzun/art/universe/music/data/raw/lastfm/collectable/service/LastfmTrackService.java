package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

import java.util.List;

public interface LastfmTrackService {

    /**
     * Find tracks for track.getInfo API call with the following priority:
     * 1. Tracks with missing playCount and listenersCount
     * 2. Prioritize tracks from popular artists (join by artist_id, sort by listenersCount)
     * 
     * @return List of tracks to process with track.getInfo API call
     */
    List<LastfmTrack> findTracksForGetInfo();
}
