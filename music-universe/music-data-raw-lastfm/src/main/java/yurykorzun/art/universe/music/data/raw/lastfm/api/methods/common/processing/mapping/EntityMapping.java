package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

/**
 * <p>Helper class holding objects that participate in inserting & updating entities with DTOs involved.</p>
 * <p>Is held by {@link EntityMappings}</p>
 * <p>Presence of <b>oldEntity</b> indicates that entity exists.</p>
 * <ul>
 *     <li>For existing entities, <b>newVersion</b> is initialized with entity from db then updated if needed</li>
 *     <li>For missing entities, <b>newVersion</b> is created from DTO</li>
 * </ul>
 *
 * @param <E> Lastfm collectable entity
 * @param <D> entity's DTO
 */
@RequiredArgsConstructor
@Getter
@Setter
public class EntityMapping<E extends BaseLastfmEntity, D extends EntityDto> {

    private final D dto;

    private E oldEntity;
    private E newEntity;

    // marks mappings containing new entities - they must be revisited to assign entity_id to their attributes
    private boolean isNew = false;

    // marks a mapping containing entity that should be saved,
    // either because it is new or because at least one of its attributes has changed
    private boolean shouldBeSaved = false;

    private EntityMappingStage stage = EntityMappingStage.NOT_INITIALIZED;

}
