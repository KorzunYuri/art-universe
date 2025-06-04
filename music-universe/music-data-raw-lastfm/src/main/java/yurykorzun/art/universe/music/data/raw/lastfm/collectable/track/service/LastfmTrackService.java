package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.List;

public interface LastfmTrackService {

    LastfmTrack saveTrack(LastfmTrack lastfmTrack);

    List<LastfmTrack> saveTracks(List<LastfmTrack> lastfmTracks);

    List<LastfmTrack> findAllByUrls(List<String> urls);

}
