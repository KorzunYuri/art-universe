package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmAttributeHistoryCandidate;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMappingResult;

import java.util.List;
import java.util.stream.Collectors;

public class AttributeHistoryBuilder {

    public static <E extends BaseLastfmEntity, D extends EntityDto<E>> List<LastfmAttributeHistoryCandidate> buildAttributeHistoryRecords(
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

    private static <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmAttributeHistoryCandidate createAttrRecord(
        EntityMapping<E, D> mapping,
        EntityAttributeHandler<E, ?, D> handler,
        LastfmApiCall apiCall
    ) {
        E entity = mapping.getNewEntity();
        long apiCallId = apiCall.getId();
        LastfmEntityType entityType = entity.getType();
        long entityId = entity.getId();

        // Determine scope if attribute is scoped
        LastfmEntityType scopeEntityType = handler.isAttributeScoped() ? apiCall.getEntityType() : null;
        Long scopeEntityId = handler.isAttributeScoped() ? apiCall.getEntityId() : null;

        // Extract and convert value based on data type
        return switch (handler.getAttribute().getDataType()) {
            case STRING -> LastfmAttributeHistoryCandidate.of(
                    apiCallId, entityType, entityId, handler.getAttribute(),
                    scopeEntityType, scopeEntityId,
                    (String) handler.extractFrom(mapping), null
            );
            case NUMERIC -> LastfmAttributeHistoryCandidate.of(
                    apiCallId, entityType, entityId, handler.getAttribute(),
                    scopeEntityType, scopeEntityId,
                    null, toNumericValue(handler.extractFrom(mapping))
            );
            case BOOLEAN -> LastfmAttributeHistoryCandidate.of(
                    apiCallId, entityType, entityId, handler.getAttribute(),
                    scopeEntityType, scopeEntityId,
                    null, (boolean) handler.extractFrom(mapping) ? 1L : 0L
            );
        };
    }

    private static Long toNumericValue(Object value) {
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        return (Long) value;
    }
}