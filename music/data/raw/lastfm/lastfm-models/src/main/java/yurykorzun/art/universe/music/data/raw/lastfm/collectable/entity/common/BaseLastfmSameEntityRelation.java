package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseLastfmSameEntityRelation<E extends BaseLastfmEntity>
        extends BaseLastfmEntityRelation<E, E> {

    public abstract LastfmEntityRelationType getRelationType();

}
