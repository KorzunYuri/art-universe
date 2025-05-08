package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

@Component
class LastfmTagTopTracksTrackFactory extends LastfmTrackEntityFactory<TagTopTracksTrackDto> {

    @Override
    protected LastfmTrack.LastfmTrackBuilder<?, ?> setExtensionFields(LastfmTrack.LastfmTrackBuilder<?, ?> builder, TagTopTracksTrackDto dto) {
        return builder
            .duration(dto.getDuration())
            .isStreamable(1 == dto.getStreamableObject().getFullTrack());
    }
}
