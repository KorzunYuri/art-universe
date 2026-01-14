package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.getinfo;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

@Component
public class LastfmArtistGetInfoArtistFactory extends LastfmArtistEntityFactory<ArtistGetInfoArtistDto> {

    @Override
    protected LastfmArtist.LastfmArtistBuilder<?, ?> setExtensionFields(LastfmArtist.LastfmArtistBuilder<?, ?> builder, ArtistGetInfoArtistDto dto) {
        return builder
            .isPrimary(true) // this is helpful for artists deduplication
            .listenersCount(dto.getStats().getListeners())
            .playCount(dto.getStats().getPlayCount())
            ;
    }
}
