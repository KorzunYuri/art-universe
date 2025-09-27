package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;

import java.util.Objects;

/**
 * Class contains common util methods for extracting attributes from entities and DTOs and setting new values to entities for saving.
 * @param <E>   entity
 * @param <T>   value type, transient & irrelevant for class logic
 * @param <D>   dto
 */
public abstract class EntityAttributeHandler<E extends BaseLastfmEntity, T, D extends EntityDto<E>> {

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
        // CONSTANT attributes should never create history records
        if (getAttribute().getHistoryType() == LastfmAttribute.HistoryType.CONSTANT) {
            return false;
        }
        
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
        if (getAttribute().getHistoryType() == LastfmAttribute.HistoryType.CONSTANT) {
            return shouldUpdateConstantAttribute(mapping.getOldEntity(), mapping.getDto());
        }
        if (getAttribute().getHistoryType() == LastfmAttribute.HistoryType.INCREASING) {
            return shouldUpdateIncreasingAttribute(mapping.getOldEntity(), mapping.getDto());
        }
        return hasAttributeChanged(mapping.getOldEntity(), mapping.getDto());
    }

    /**
     * For CONSTANT attributes, only update if the old value is null or empty.
     * This prevents overwriting existing URLs/MBIDs with different encodings.
     */
    private boolean shouldUpdateConstantAttribute(@Nullable E oldEntity, D dto) {
        if (oldEntity == null) {
            return extractFrom(dto) != null; // New entity, always set value
        }
        
        return isNullOrEmpty(extractFrom(oldEntity))
            && !isNullOrEmpty(extractFrom(dto));
    }

    /**
     * For INCREASING attributes, only update if the new value is bigger than the old value or old value is null/empty.
     * This ensures that counters (listeners_count, play_count, etc.) only increase over time.
     */
    private boolean shouldUpdateIncreasingAttribute(@Nullable E oldEntity, D dto) {
        if (oldEntity == null) {
            return extractFrom(dto) != null; // New entity, always set value if available
        }

        T oldValueRaw = extractFrom(oldEntity);
        T newValueRaw = extractFrom(dto);
        
        // Convert to Long for comparison, handling both Integer and Long types
        Long oldValue = convertToLong(oldValueRaw);
        Long newValue = convertToLong(newValueRaw);
        
        // If new value is null, don't update
        if (newValue == null) {
            return false;
        }
        
        // If old value is null or zero, update with any positive new value
        if (oldValue == null || oldValue <= 0) {
            return newValue > 0;
        }
        
        // Only update if new value is greater than old value
        return newValue > oldValue;
    }

    /**
     * Safely converts numeric values to Long for comparison.
     * Handles both Integer and Long types commonly used in numeric attributes.
     */
    private Long convertToLong(T value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Cannot convert value to Long: " + value.getClass().getSimpleName());
    }

    /**
     * Checks if a value is null or empty (for strings).
     */
    private boolean isNullOrEmpty(T value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        return false;
    }

}
