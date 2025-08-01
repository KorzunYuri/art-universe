package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.processing.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

@Component
public class LastfmArtistTopTagsTagFactory extends LastfmTagEntityFactory<ArtistTopTagsTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, ArtistTopTagsTagDto dto) {
        return builder
            .url(dto.getUrl())
            ;
    }
}
