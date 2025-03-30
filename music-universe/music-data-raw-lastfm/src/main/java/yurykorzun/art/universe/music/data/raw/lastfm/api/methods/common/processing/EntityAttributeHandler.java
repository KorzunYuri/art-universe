package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

public interface EntityAttributeHandler<E extends BaseLastfmEntity, T, D> {
    LastfmAttribute getAttribute();
    boolean isAttributeEmbedded();
    boolean isAttributeScoped();
    boolean shouldCreateNewValueUnconditionally();
    T extractFrom(E entity);
    T extractFrom(D dto);
    void copyTo(E dest, D src);
    void copyTo(E dest, E src);
}
