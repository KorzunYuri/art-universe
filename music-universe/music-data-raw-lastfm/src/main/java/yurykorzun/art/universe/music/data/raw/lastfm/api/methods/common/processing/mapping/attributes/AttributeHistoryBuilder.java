package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappings;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;

public interface AttributeHistoryBuilder<E extends BaseLastfmEntity, D extends EntityDto> {

    /**
     * Creates attribute history records from mappings.
     */
    List<LastfmAttributeHistoryRecord> buildAttributeHistoryRecords(
        EntityMappings<E, D> mappings,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        LastfmApiCall apiCall
    );
}