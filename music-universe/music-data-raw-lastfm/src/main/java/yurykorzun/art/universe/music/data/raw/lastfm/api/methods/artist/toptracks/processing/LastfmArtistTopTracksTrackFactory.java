package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

@Component
public class LastfmArtistTopTracksTrackFactory extends LastfmTrackEntityFactory<ArtistTopTracksTrackDto> {

    @Override
    protected LastfmTrack.LastfmTrackBuilder<?, ?> setExtensionFields(LastfmTrack.LastfmTrackBuilder<?, ?> builder, ArtistTopTracksTrackDto dto) {
        return builder
            .streamable(1 == dto.getStreamable())
            .playCount(dto.getPlayCount())
            .listenersCount(dto.getListenersCount());
    }
}
