package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmEntityRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(LastfmEntityRelationServiceImpl.class)
class LastfmEntityRelationServiceImplTest extends JpaOnlyTest {

    @Autowired
    private LastfmEntityRelationServiceImpl entityRelationService;

    @Autowired
    private LastfmEntityRelationRepository entityRelationRepository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    private LastfmDataSnapshot createDummyDataSnapshot() {
        return snapshotRepository.save(new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, new Date()));
    }

    private LastfmApiCall createDummyApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot();
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
            .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    private BaseLastfmEntity createDummyEntity() {
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmTag tag = LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .apiCall(dummyApiCall)
            .build();
        return tagRepository.save(tag);
    }

    private LastfmEntityRelation buildEntityRelation(
            BaseLastfmEntity scopeEntity,
            BaseLastfmEntity entity,
            LastfmApiCall sourceApiCall
    ) {
        return LastfmEntityRelation.builder()
                .scopeEntityType((LastfmEntityType) scopeEntity.getType())
                .scopeEntityId(scopeEntity.getId())
                .entityType((LastfmEntityType) entity.getType())
                .entityId(entity.getId())
                .apiCall(sourceApiCall)
            .build();
    }

    @Test
    void givenNewEntityRelation_whenPersistedTwice_thenNotDuplicated() {
        //  given new entity relation
        BaseLastfmEntity scopeEntity = createDummyEntity();
        BaseLastfmEntity entity = createDummyEntity();
        LastfmApiCall sourceApiCall = createDummyApiCall();
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
            BaseLastfmEntity scopeEntity = createDummyEntity();
            BaseLastfmEntity entity = createDummyEntity();
            LastfmApiCall sourceApiCall = createDummyApiCall();
            relations.add(buildEntityRelation(scopeEntity, entity, sourceApiCall));
        }

        // when persisted
        entityManager.flush();
        entityRelationService.upsertEntityRelations(relations);

        // then saved correctly
        assertEquals(relations.size(), entityRelationRepository.findAll().size());

        // when partly persisted twice
        BaseLastfmEntity scopeEntity = createDummyEntity();
        BaseLastfmEntity entity = createDummyEntity();
        LastfmApiCall sourceApiCall = createDummyApiCall();
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