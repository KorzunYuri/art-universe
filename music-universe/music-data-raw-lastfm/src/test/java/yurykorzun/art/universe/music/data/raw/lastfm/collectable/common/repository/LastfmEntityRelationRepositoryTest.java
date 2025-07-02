package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmEntityRelationRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmEntityRelationRepository entityRelationRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Test
    void newEntityRelation_shouldNotBeDuplicated_whenPersistedTwice() {
        //  given new entity relation
        BaseLastfmEntity scopeEntity = consistencyHelper.createAndSaveDummyEntity();
        BaseLastfmEntity entity = consistencyHelper.createAndSaveDummyEntity();
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall();
        LastfmEntityRelation entityRelation = LastfmEntityRelation.builder()
                .scopeEntityType(scopeEntity.getType())
                .scopeEntityId(scopeEntity.getId())
                .entityType(entity.getType())
                .entityId(entity.getId())
                .apiCall(sourceApiCall)
            .build();

        // when persisted
        entityRelationRepository.upsertEntityRelation(entityRelation);

        // then saved correctly
        List<LastfmEntityRelation> entityRelations = entityRelationRepository.findAll();
        assertEquals(1, entityRelations.size());
        LastfmEntityRelation savedRelation = entityRelations.get(0);
        assertEquals(entity.getId(), savedRelation.getEntityId());
        assertEquals(entity.getType(), savedRelation.getEntityType());
        assertEquals(scopeEntity.getId(), savedRelation.getScopeEntityId());
        assertEquals(scopeEntity.getType(), savedRelation.getScopeEntityType());
        assertEquals(sourceApiCall, savedRelation.getApiCall());

        // when persisted twice
        entityRelationRepository.upsertEntityRelation(entityRelation);

        // then not duplicated
        assertEquals(1, entityRelations.size());
    }

}