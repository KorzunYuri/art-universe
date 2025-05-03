package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

@Component
class LastfmArtistGetInfoArtistFactory extends LastfmArtistEntityFactory<ArtistGetInfoArtistDto> {

    @Override
    protected LastfmArtist.LastfmArtistBuilder<?, ?> setExtensionFields(LastfmArtist.LastfmArtistBuilder<?, ?> builder, ArtistGetInfoArtistDto dto) {
        return builder
            .isStreamable(1 == dto.getStreamable())
            .isOnTour(1 == dto.getOnTour())
            .listenersCount(dto.getStats().getListeners())
            .playCount(dto.getStats().getPlayCount())
            ;
    }
}
