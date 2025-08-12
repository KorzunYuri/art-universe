package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.processing.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto.TrackGetInfoTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

class LastfmTrackGetInfoTrackFactory extends LastfmTrackEntityFactory<TrackGetInfoTrackDto> {

    private final LastfmArtist artist;

    LastfmTrackGetInfoTrackFactory(LastfmArtist artist) {
        this.artist = artist;
    }

    @Override
    protected LastfmTrack.LastfmTrackBuilder<?, ?> setExtensionFields(LastfmTrack.LastfmTrackBuilder<?, ?> builder, TrackGetInfoTrackDto dto) {
        return builder
            .artist(artist)
            .duration(dto.getDuration())
            .listenersCount(dto.getListenersCount())
            .playCount(dto.getPlayCount());
    }
}
