package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntityRelationBuilder<E extends BaseLastfmEntity, D extends EntityDto> {

    /**
     * Create entity relations, if relevant (i.e., if the api call had the scope entity)
     */
    public List<LastfmEntityRelation> buildEntityRelations(
        EntityMappings<E, D> mappings,
        LastfmApiCall apiCall
    ) {
        if (apiCall.getEntityId() == 0) {
            return Collections.emptyList();
        }
        return mappings.values().stream()
            .map(mapping -> LastfmEntityRelation.builder()
                    .apiCall(apiCall)
                    .scopeEntityType(apiCall.getEntityType())
                    .scopeEntityId(apiCall.getEntityId())
                    .entityType(mapping.getNewEntity().getType())
                    .entityId(mapping.getNewEntity().getId())
                .build())
            .collect(Collectors.toList());
    }
}