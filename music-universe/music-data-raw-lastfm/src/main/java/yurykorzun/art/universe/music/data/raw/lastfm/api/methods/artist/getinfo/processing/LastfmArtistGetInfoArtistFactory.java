package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

@Component
class LastfmArtistGetInfoArtistFactory extends LastfmArtistEntityFactory<ArtistGetInfoArtistDto> {

    @Override
    protected LastfmArtist.LastfmArtistBuilder<?, ?> setExtensionFields(LastfmArtist.LastfmArtistBuilder<?, ?> builder, ArtistGetInfoArtistDto dto) {
        return builder
            .isPrimary(true) // this is helpful for artists deduplication
            .listenersCount(dto.getStats().getListeners())
            .playCount(dto.getStats().getPlayCount())
            ;
    }
}
