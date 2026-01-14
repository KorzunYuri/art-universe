package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

public class LastfmTagEntityFactory <D extends TagDto> implements EntityFactory<LastfmTag, D> {

    @Override
    public LastfmTag fromDto(D dto, LastfmApiCall sourceApiCall) {
        return setExtensionFields(
            LastfmTag.builder()
                .apiCall(sourceApiCall)
                .name(dto.getName()),
            dto
        ).build();
    }

    protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, D dto) {
        return builder;
    }

    @Override
    public LastfmTag clone(LastfmTag entity) {
        return LastfmTag.builder()
                .apiCall(entity.getApiCall())
                .name(entity.getName())
                .id(entity.getId())
                .approvalStatus(entity.getApprovalStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
