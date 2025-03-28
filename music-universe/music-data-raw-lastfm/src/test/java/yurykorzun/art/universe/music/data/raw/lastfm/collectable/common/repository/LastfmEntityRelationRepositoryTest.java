package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LastfmEntityRelationRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmEntityRelationRepository entityRelationRepository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

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
        LastfmTag tag = LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .apiCall(createDummyApiCall())
            .build();
        return tagRepository.save(tag);
    }

    @Test
    void givenNewEntityRelation_whenPersistedTwice_thenNotDuplicated() {
        //  given new entity relation
        BaseLastfmEntity scopeEntity = createDummyEntity();
        BaseLastfmEntity entity = createDummyEntity();
        LastfmApiCall sourceApiCall = createDummyApiCall();
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