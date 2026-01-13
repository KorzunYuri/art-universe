package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.album.getinfo;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo.AlbumGetInfoTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

@Component
public class LastfmAlbumGetInfoTagFactory extends LastfmTagEntityFactory<AlbumGetInfoTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, AlbumGetInfoTagDto dto) {
        return builder
            .url(dto.getUrl());
    }
}