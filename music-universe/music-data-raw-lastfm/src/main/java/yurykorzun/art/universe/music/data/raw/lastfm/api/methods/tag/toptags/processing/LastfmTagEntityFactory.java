package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagDtoWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

public class LastfmTagEntityFactory implements EntityFactory<LastfmTag, TagDtoWrapper> {

    @Override
    public LastfmTag fromDto(TagDtoWrapper dto, LastfmApiResponse response) {
        return LastfmTag.builder()
                .apiCall(response.getApiCall())
                .name(dto.getName())
            .build();
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
