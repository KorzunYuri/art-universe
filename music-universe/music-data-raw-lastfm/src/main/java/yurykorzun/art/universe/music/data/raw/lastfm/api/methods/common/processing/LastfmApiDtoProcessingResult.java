package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;

import java.util.List;

public record LastfmApiDtoProcessingResult<E extends BaseLastfmEntity, D extends EntityDto<E>>(
    List<E> actualEntities,
    List<LastfmAttributeHistoryRecord> savedAttributeValues,
    EntityMappingResult<E, D> entityMapping
) {

    public static <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmApiDtoProcessingResult<E, D> empty(LastfmApiCall sourceApiCall) {
        return new LastfmApiDtoProcessingResult<>(List.of(), List.of(), new EntityMappingResult<>(java.util.Map.of(), sourceApiCall));
    }

}