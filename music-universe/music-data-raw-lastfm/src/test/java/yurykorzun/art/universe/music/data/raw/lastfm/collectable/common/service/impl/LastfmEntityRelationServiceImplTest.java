package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmEntityRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(LastfmEntityRelationServiceImpl.class)
class LastfmEntityRelationServiceImplTest extends JpaOnlyTest {

    @Autowired
    private LastfmEntityRelationServiceImpl entityRelationService;

    @Autowired
    private LastfmEntityRelationRepository entityRelationRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;

    private LastfmEntityRelation buildEntityRelation(
            BaseLastfmEntity scopeEntity,
            BaseLastfmEntity entity,
            LastfmApiCall sourceApiCall
    ) {
        return LastfmEntityRelation.builder()
                .scopeEntityType(scopeEntity.getType())
                .scopeEntityId(scopeEntity.getId())
                .entityType(entity.getType())
                .entityId(entity.getId())
                .apiCall(sourceApiCall)
            .build();
    }

    @Test
    void givenNewEntityRelation_whenPersistedTwice_thenNotDuplicated() {
        //  given new entity relation
        BaseLastfmEntity scopeEntity = consistencyHelper.createDummyEntity();
        BaseLastfmEntity entity = consistencyHelper.createDummyEntity();
        LastfmApiCall sourceApiCall = consistencyHelper.createDummyApiCall();
        LastfmEntityRelation entityRelation = buildEntityRelation(scopeEntity, entity, sourceApiCall);

        // when persisted
        entityRelationService.upsertEntityRelation(entityRelation);

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
        entityRelationService.upsertEntityRelation(entityRelation);

        // then not duplicated
        assertEquals(1, entityRelations.size());
    }

    @Test
    void givenBatchOfEntityRelations_whenPersistedTwice_thenNotDuplicated() {
        // given N different relations
        List<LastfmEntityRelation> relations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BaseLastfmEntity scopeEntity = consistencyHelper.createDummyEntity();
            BaseLastfmEntity entity = consistencyHelper.createDummyEntity();
            LastfmApiCall sourceApiCall = consistencyHelper.createDummyApiCall();
            relations.add(buildEntityRelation(scopeEntity, entity, sourceApiCall));
        }

        // when persisted
        entityManager.flush();
        entityRelationService.upsertEntityRelations(relations);

        // then saved correctly
        assertEquals(relations.size(), entityRelationRepository.findAll().size());

        // when partly persisted twice
        BaseLastfmEntity scopeEntity = consistencyHelper.createDummyEntity();
        BaseLastfmEntity entity = consistencyHelper.createDummyEntity();
        LastfmApiCall sourceApiCall = consistencyHelper.createDummyApiCall();
        LastfmEntityRelation newRelation = buildEntityRelation(scopeEntity, entity, sourceApiCall);
        List<LastfmEntityRelation> newBatch = List.of(
                relations.get(0),
                newRelation
        );
        entityManager.flush();
        entityRelationService.upsertEntityRelations(newBatch);

        // then only new records are saved
        assertEquals(relations.size() + 1, entityRelationRepository.findAll().size());
    }

}