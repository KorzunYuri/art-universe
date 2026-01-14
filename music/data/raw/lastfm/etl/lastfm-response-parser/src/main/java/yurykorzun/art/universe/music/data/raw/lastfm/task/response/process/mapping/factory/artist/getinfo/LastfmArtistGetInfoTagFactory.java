package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.getinfo;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo.ArtistGetInfoArtistTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

@Component
public class LastfmArtistGetInfoTagFactory extends LastfmTagEntityFactory<ArtistGetInfoArtistTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, ArtistGetInfoArtistTagDto dto) {
        return builder
            .url(dto.getUrl());
    }
}
