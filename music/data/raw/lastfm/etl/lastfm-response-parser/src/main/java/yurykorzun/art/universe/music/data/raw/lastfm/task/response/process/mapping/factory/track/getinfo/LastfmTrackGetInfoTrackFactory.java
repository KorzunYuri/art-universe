package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.getinfo;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo.TrackGetInfoTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

public class LastfmTrackGetInfoTrackFactory extends LastfmTrackEntityFactory<TrackGetInfoTrackDto> {

    private final LastfmArtist artist;

    public LastfmTrackGetInfoTrackFactory(LastfmArtist artist) {
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
