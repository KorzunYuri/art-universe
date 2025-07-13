package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
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
@Slf4j
public class DefaultEntityAttributeHandler<E extends BaseLastfmEntity, T, D extends EntityDto<E>>
    extends EntityAttributeHandler<E, T, D> {

    @Getter
    private final LastfmAttribute attribute;
    @Getter
    private final boolean isAttributeEmbedded;
    @Getter
    private final boolean isAttributeScoped;

    private final Function<E, T> entityValueExtractor;
    private final BiConsumer<E, T> entityValueSetter;
    private final Function<D, T> dtoValueExtractor;

    private static <T> T throwUnsupported(String message) {
        throw new UnsupportedOperationException(message);
    };

    public static <E extends BaseLastfmEntity, T, D extends EntityDto<E>> DefaultEntityAttributeHandler<E, T, D> forEmbeddedAttribute(
        LastfmAttribute attribute,
        boolean isAttributeScoped,
        Function<E, T> entityValueExtractor,
        BiConsumer<E, T> entityValueSetter,
        Function<D, T> dtoValueExtractor
    ) {
        return new DefaultEntityAttributeHandler<>(
            attribute, true, isAttributeScoped,
            entityValueExtractor,
            entityValueSetter,
            dtoValueExtractor);
    }

    public static <E extends BaseLastfmEntity, T, D extends EntityDto<E>> DefaultEntityAttributeHandler<E, T, D> forExternalAttribute(
        LastfmAttribute attribute,
        boolean isAttributeScoped,
        Function<D, T> dtoValueExtractor
    ) {
        return new DefaultEntityAttributeHandler<>(
            attribute, false, isAttributeScoped,
            (e) -> throwUnsupported(String.format("External attribute %s cannot be extracted from entity", attribute)),
            (e, t) -> throwUnsupported(String.format("External attribute %s cannot be set to entity", attribute)),
            dtoValueExtractor);
    }

    private DefaultEntityAttributeHandler(
        LastfmAttribute attribute, boolean isAttributeEmbedded, boolean isAttributeScoped,
        Function<E, T> entityValueExtractor, BiConsumer<E, T> entityValueSetter,
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
    public T extractFrom(E entity) {
        return entityValueExtractor.apply(entity);
    }

    @Override
    public T extractFrom(D dto) {
        return dtoValueExtractor.apply(dto);
    }

    @Override
    public void copyTo(E dest, E src) {
        this.entityValueSetter.accept(dest, entityValueExtractor.apply(src));
    }

    @Override
    public void copyTo(E entity, D dto) {
        this.entityValueSetter.accept(entity, dtoValueExtractor.apply(dto));
    }
}
