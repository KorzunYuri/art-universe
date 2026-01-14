package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.toptags;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptags.ArtistTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

@Component
public class LastfmArtistTopTagsTagFactory extends LastfmTagEntityFactory<ArtistTopTagsTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, ArtistTopTagsTagDto dto) {
        return builder
            .url(dto.getUrl())
            ;
    }
}
