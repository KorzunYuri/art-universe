package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.getinfo;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo.TrackGetInfoTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

@Component
public class LastfmTrackGetInfoTagFactory extends LastfmTagEntityFactory<TrackGetInfoTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, TrackGetInfoTagDto dto) {
        return builder
            .url(dto.getUrl());
    }
}
