package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.processing.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

@Component
class LastfmTagTopTagsTagFactory extends LastfmTagEntityFactory<TagTopTagsTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, TagTopTagsTagDto dto) {
        return builder
            .usageCount(dto.getUsageCount())
            .usageUsersCount(dto.getUsageUsersCount());
    }
}
