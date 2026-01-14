package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.search;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.search.ArtistSearchArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

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
