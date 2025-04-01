package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;

import java.util.List;

public record LastfmApiDtoProcessingResult<E extends BaseLastfmEntity>(
    List<E> savedEntities,
    List<LastfmAttributeHistoryRecord> attributeHistories,
    List<LastfmEntityRelation> entityRelations
) {
}