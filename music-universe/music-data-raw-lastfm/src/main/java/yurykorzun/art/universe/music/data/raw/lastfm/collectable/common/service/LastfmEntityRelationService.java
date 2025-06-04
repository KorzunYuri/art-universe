package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;

import java.util.List;

public interface LastfmEntityRelationService {

    void upsertEntityRelation(LastfmEntityRelation entityRelation);
    void upsertEntityRelations(List<LastfmEntityRelation> entityRelations);

}
