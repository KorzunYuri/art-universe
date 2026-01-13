package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.toptags;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptags.TagTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

@Component
public class LastfmTagTopTagsTagFactory extends LastfmTagEntityFactory<TagTopTagsTagDto> {

    @Override
    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, TagTopTagsTagDto dto) {
        return builder
            .usageCount(dto.getUsageCount())
            .usageUsersCount(dto.getUsageUsersCount());
    }
}
