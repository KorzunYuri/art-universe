package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import lombok.Getter;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Helper class for unifying entities and DTOs attribute handling. Use cases:
 * <ul>
 *     <ol>extract value from entity</ol>
 *     <ol>extract value from DTO and copy if to entity</ol>
 * </ul>
 * </p>
 * @param <E>   entity
 * @param <T>   value
 * @param <D>   DTO
 */
public class HistoryAttributeHandler<E extends BaseLastfmEntity, T, D> implements EntityAttributeHandler<E, T, D> {

    @Getter
    private final LastfmAttribute attribute;
    @Getter
    private final boolean isAttributeEmbedded;
    @Getter
    private final boolean isAttributeScoped;

    private final Function<E, T> entityValueExtractor;
    private final BiConsumer<E, T> entityValueSetter;
    private final Function<D, T> dtoValueExtractor;

    public HistoryAttributeHandler(
        LastfmAttribute attribute,
        boolean isAttributeEmbedded, boolean isAttributeScoped,
        Function<E, T> entityValueExtractor,
        BiConsumer<E, T> entityValueSetter,
        Function<D, T> dtoValueExtractor
    ) {
        this.attribute = attribute;
        this.isAttributeEmbedded = isAttributeEmbedded;
        this.isAttributeScoped = isAttributeScoped;
        this.entityValueExtractor = entityValueExtractor;
        this.entityValueSetter = entityValueSetter;
        this.dtoValueExtractor = dtoValueExtractor;
    }

    @Override
    public boolean shouldCreateNewValueUnconditionally() {
        return LastfmAttribute.HistoryType.SNAPSHOT == attribute.getHistoryType();
    }

    @Override
    public T extractFrom(E entity) {
        return entityValueExtractor.apply(entity);
    }

    @Override
    public T extractFrom(D dto) {
        return dtoValueExtractor.apply(dto);
    }

    @Override
    public void copyTo(E dest, E src) {
        this.entityValueSetter.accept(dest, entityValueExtractor.apply(dest));
    }

    @Override
    public void copyTo(E entity, D dto) {
        this.entityValueSetter.accept(entity, dtoValueExtractor.apply(dto));
    }
}
