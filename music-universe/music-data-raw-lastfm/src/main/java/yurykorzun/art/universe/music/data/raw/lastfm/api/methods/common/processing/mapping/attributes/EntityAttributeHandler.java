package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.Objects;

/**
 * Class contains common util methods for extracting attributes from entities and DTOs and setting new values to entities for saving.
 * @param <E>   entity
 * @param <T>   value type, transient & irrelevant for class logic
 * @param <D>   dto
 */
public abstract class EntityAttributeHandler<E extends BaseLastfmEntity, T, D extends EntityDto> {

    public abstract LastfmAttribute getAttribute();

    /**
     * <p>Indicates whether attribute belongs to the entity itself or is external.</p>
     * <p>External attribute value cannot be extracted/set from/to an entity. It only can be extracted from DTO
     * to produce a new attribute_history record.</p>
     */
    // Non-embedded value cannot be extracted/set from/to an entity.
    public abstract boolean isAttributeEmbedded();

    public boolean isAttributeExternal() {
        return !isAttributeEmbedded();
    }

    // does attribute belong to an entity relation, i.e. has a 'scope' entity
    public abstract boolean isAttributeScoped();

    protected abstract @Nullable T extractFrom(E entity);

    protected abstract @Nullable T extractFrom(D dto);

    protected @Nullable T extractFrom(EntityMapping<E, D> mapping) {
        return extractFrom(mapping.getDto());
    }

    public abstract void copyTo(E dest, E src);

    public abstract void copyTo(E dest, D src);

    protected boolean shouldCreateNewValueUnconditionally() {
        return LastfmAttribute.HistoryType.SNAPSHOT == getAttribute().getHistoryType();
    }

    private boolean shouldCreateNewRecord(@Nullable E oldEntity, D newDto) {
        return shouldCreateNewValueUnconditionally()
            || isAttributeExternal() // cannot extract external attribute from entity, thus cannot compare
            || hasAttributeChanged(oldEntity, newDto);
    }

    public boolean shouldCreateNewRecord(EntityMapping<E, D> mapping) {
        return shouldCreateNewRecord(mapping.getOldEntity(), mapping.getDto());
    }

    // TODO make sure not to call extraction for external attributes (there is no protection atm)
    private boolean hasAttributeChanged(@Nullable E oldEntity, D newDto) {
        return oldEntity == null
            || !Objects.equals(extractFrom(oldEntity), extractFrom(newDto));
    }

    public boolean hasAttributeChanged(EntityMapping<E, D> mapping) {
        return hasAttributeChanged(mapping.getOldEntity(), mapping.getDto());
    }

}
