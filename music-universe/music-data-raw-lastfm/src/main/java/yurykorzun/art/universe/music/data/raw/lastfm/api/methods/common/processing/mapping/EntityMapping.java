package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;

/**
 * <p>Helper class holding objects that participate in inserting & updating entities with DTOs involved.</p>
 * <p>Is held by {@link EntityMappingResult}</p>
 * <p>Presence of <b>oldEntity</b> indicates that entity exists.</p>
 * <ul>
 *     <li>For existing entities, <b>newEntity</b> is initialized with entity from db then updated if needed</li>
 *     <li>For missing entities, <b>newEntity</b> is created from DTO</li>
 * </ul>
 *
 * @param <E> Lastfm collectable entity
 * @param <D> entity's DTO
 */
@RequiredArgsConstructor
@Getter
@Setter
public class EntityMapping<E extends BaseLastfmEntity, D extends EntityDto<E>> {

    private final D dto;

    private E oldEntity;      // Clone of original entity (for change tracking)
    private E newEntity;      // Current version of entity (may be updated)

    private boolean isNew = false;
    private boolean shouldBeSaved = false;
    private EntityMappingStage stage = EntityMappingStage.NOT_INITIALIZED;

}
