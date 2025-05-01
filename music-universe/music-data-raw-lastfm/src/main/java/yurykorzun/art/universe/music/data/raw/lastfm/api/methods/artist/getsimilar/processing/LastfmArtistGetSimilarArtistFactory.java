package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

@Component
public class LastfmArtistGetSimilarArtistFactory extends LastfmArtistEntityFactory<ArtistGetSimilarArtistDto> {

    @Override
    protected LastfmArtist.LastfmArtistBuilder<?, ?> setExtensionFields(
        LastfmArtist.LastfmArtistBuilder<?, ?> builder, ArtistGetSimilarArtistDto dto
    ) {
        return builder
            .isStreamable(1 == dto.getStreamable());
    }
}
