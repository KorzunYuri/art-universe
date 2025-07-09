package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;
import java.util.stream.Collectors;

public class AttributeHistoryBuilder {

    public static <E extends BaseLastfmEntity, D extends EntityDto<E>> List<LastfmAttributeHistoryRecord> buildAttributeHistoryRecords(
        EntityMappingResult<E, D> mappings,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        LastfmApiCall apiCall
    ) {
        return mappings.values().stream()
            .flatMap(mapping ->
                attrHandlers.stream()
                    .filter(handler -> handler.shouldCreateNewRecord(mapping))
                    .map(handler -> createAttrRecord(mapping, handler, apiCall))
            )
            .collect(Collectors.toList());
    }

    private static <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmAttributeHistoryRecord createAttrRecord(
        EntityMapping<E, D> mapping,
        EntityAttributeHandler<E, ?, D> handler,
        LastfmApiCall apiCall
    ) {
        E entity = mapping.getNewEntity();
        LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder builder = LastfmAttributeHistoryRecord.builder()
            .attribute(handler.getAttribute())
            .apiCallId(apiCall.getId())
            .entityType(entity.getType())
            .entityId(entity.getId());

        if (handler.isAttributeScoped()) {
            builder .scopeEntityType(apiCall.getEntityType())
                    .scopeEntityId(apiCall.getEntityId());
        }

        switch (handler.getAttribute().getDataType()) {
            case STRING:
                builder.stringValue((String) handler.extractFrom(mapping));
                break;
            case INTEGER:
                builder.intValue((Integer) handler.extractFrom(mapping));
                break;
            case BOOLEAN:
                builder.intValue((boolean) handler.extractFrom(mapping) ? 1 : 0);
                break;
        }
        return builder.build();
    }
}