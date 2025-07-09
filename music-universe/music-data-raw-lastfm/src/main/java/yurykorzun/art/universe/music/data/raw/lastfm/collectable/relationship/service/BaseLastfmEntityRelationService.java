package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntityRelation;

import java.util.List;

public interface BaseLastfmEntityRelationService<R extends BaseLastfmEntityRelation<? extends BaseLastfmEntity, ? extends BaseLastfmEntity>> {

    @Transactional
    List<R> upsertAll(List<R> relations);

}
