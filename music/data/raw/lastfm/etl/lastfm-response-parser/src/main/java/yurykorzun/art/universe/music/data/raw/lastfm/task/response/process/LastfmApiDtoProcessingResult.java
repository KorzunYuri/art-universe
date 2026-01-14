package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;

import java.util.List;

public record LastfmApiDtoProcessingResult<E extends BaseLastfmEntity, D extends EntityDto<E>>(
    List<E> actualEntities,
    int savedAttributeRecordsCount,
    EntityMappingResult<E, D> entityMapping
) {

    public static <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmApiDtoProcessingResult<E, D> empty(LastfmApiCall sourceApiCall) {
        return new LastfmApiDtoProcessingResult<>(List.of(), 0, new EntityMappingResult<>(java.util.Map.of(), sourceApiCall));
    }

}