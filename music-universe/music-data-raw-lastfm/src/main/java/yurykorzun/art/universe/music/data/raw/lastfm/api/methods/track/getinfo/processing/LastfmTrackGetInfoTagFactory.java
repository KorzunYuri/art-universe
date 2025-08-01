package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto.TrackGetInfoTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

@Component
class LastfmTrackGetInfoTagFactory extends LastfmTagEntityFactory<TrackGetInfoTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, TrackGetInfoTagDto dto) {
        return builder
            .url(dto.getUrl());
    }
}
