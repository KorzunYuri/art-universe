package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.processing.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

@Component
public class LastfmAlbumGetInfoTagFactory extends LastfmTagEntityFactory<AlbumGetInfoTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, AlbumGetInfoTagDto dto) {
        return builder
            .url(dto.getUrl());
    }
}