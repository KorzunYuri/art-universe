package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.util.List;

@Service
public class LastfmTrackServiceImpl implements LastfmTrackService {

    private final LastfmTrackRepository trackRepository;

    public LastfmTrackServiceImpl(LastfmTrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public LastfmTrack saveTrack(LastfmTrack lastfmTrack) {
        return trackRepository.save(lastfmTrack);
    }

    @Override
    public List<LastfmTrack> saveTracks(List<LastfmTrack> lastfmTracks) {
        return trackRepository.saveAll(lastfmTracks);
    }

    @Override
    public List<LastfmTrack> findAllByUrls(List<String> urls) {
        return trackRepository.findAllByUrlIn(urls);
    }
}
