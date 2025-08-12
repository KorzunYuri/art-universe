package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

@Component
public class LastfmArtistSearchArtistFactory extends LastfmArtistEntityFactory<ArtistSearchArtistDto> {

    @Override
    protected LastfmArtist.LastfmArtistBuilder<?, ?> setExtensionFields(LastfmArtist.LastfmArtistBuilder<?, ?> builder, ArtistSearchArtistDto dto) {
        return builder
            .listenersCount(dto.getListenersCount())
            .approvalStatus(ApprovalStatus.PRE_APPROVED)
        ;
    }
}
