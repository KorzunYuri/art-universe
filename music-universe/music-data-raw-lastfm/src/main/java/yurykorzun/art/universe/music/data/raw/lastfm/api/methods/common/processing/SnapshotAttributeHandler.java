package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import jakarta.annotation.Nullable;
import lombok.Getter;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.function.Function;

public class SnapshotAttributeHandler<E extends BaseLastfmEntity, T, D> implements EntityAttributeHandler <E, T, D> {

    @Getter
    private final LastfmAttribute attribute;
    @Getter
    private final boolean isAttributeEmbedded;
    @Getter
    private final boolean isAttributeScoped;

    private final Function<D, T> dtoValueExtractor;

    public SnapshotAttributeHandler(
        LastfmAttribute attribute,
        boolean isAttributeEmbedded, boolean isAttributeScoped,
        Function<D, T> dtoValueExtractor
    ) {
        this.attribute = attribute;
        this.isAttributeEmbedded = isAttributeEmbedded;
        this.isAttributeScoped = isAttributeScoped;
        this.dtoValueExtractor = dtoValueExtractor;
    }

    @Override
    public boolean shouldCreateNewValueUnconditionally() {
        return LastfmAttribute.HistoryType.SCD2 == attribute.getHistoryType();
    }

    @Nullable
    @Override
    public T extractFrom(E entity) {
        return null;
    }

    @Override
    public T extractFrom(D dto) {
        return dtoValueExtractor.apply(dto);
    }

    @Override
    public void copyTo(E dest, D src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyTo(E dest, E src) {
        throw new UnsupportedOperationException();
    }
}
