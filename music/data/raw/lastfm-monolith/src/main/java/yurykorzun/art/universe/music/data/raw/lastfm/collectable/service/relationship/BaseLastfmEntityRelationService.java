package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship;

import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntityRelation;

import java.util.List;

public interface BaseLastfmEntityRelationService<R extends BaseLastfmEntityRelation<? extends BaseLastfmEntity, ? extends BaseLastfmEntity>> {

    @Transactional
    void upsertAll(List<R> relations);

}
