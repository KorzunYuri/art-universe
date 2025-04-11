package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

class LastfmArtistGetInfoTagFactory extends LastfmTagEntityFactory<ArtistGetInfoArtistTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, ArtistGetInfoArtistTagDto dto) {
        return builder
            .url(dto.getUrl());
    }
}
