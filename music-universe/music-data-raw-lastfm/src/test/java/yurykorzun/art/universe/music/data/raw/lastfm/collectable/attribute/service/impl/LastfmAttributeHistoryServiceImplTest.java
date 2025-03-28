package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Import(value = {
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
})
public class LastfmAttributeHistoryServiceImplTest extends JpaOnlyTest {

    @Autowired
    private LastfmAttributeHistoryService service;

    @Autowired
    private LastfmAttributeHistoryRecordRepository repository;

    // injections for creating consistent state in db

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
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmTag tag = LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .apiCall(dummyApiCall)
            .build();
        return tagRepository.save(tag);
    }

    private static LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder initAttrValueBuilder(
        BaseLastfmEntity entity,
        BaseLastfmEntity scopeEntity,
        LastfmApiCall apiCall
    ) {
        return LastfmAttributeHistoryRecord.builder()
            .apiCallId(apiCall.getId())
            .entityType(entity.getType())
            .entityId(entity.getId())
            .scopeEntityType(scopeEntity.getType())
            .scopeEntityId(scopeEntity.getId());
    }

    private static final LastfmAttribute SCD2_ATTRIBUTE = LastfmAttribute.RELATIONS_COUNT;
    private static final LastfmAttribute SNAPSHOT_ATTRIBUTE = LastfmAttribute.RANK;

    @Test
    void givenEmptuDb_whenUpsertedAttrValue_thenRecordIsSaved() {
        LastfmAttributeHistoryRecord candidate = initAttrValueBuilder(createDummyEntity(), createDummyEntity(), createDummyApiCall())
                .attribute(SCD2_ATTRIBUTE)
                .intValue(42)
            .build();
        LastfmAttributeHistoryRecord result = service.upsertCandidateValue(candidate);
        assertNotNull(result);
        assertTrue(result.getId() > 0);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void givenSCD2AttrValue_whenUpsertedTwice_thenNotDuplicated() {
        LastfmAttributeHistoryRecord candidate = initAttrValueBuilder(createDummyEntity(), createDummyEntity(), createDummyApiCall())
                .attribute(SCD2_ATTRIBUTE)
                .intValue(42)
            .build();
        LastfmAttributeHistoryRecord saved = service.upsertCandidateValue(candidate);
        LastfmAttributeHistoryRecord result = service.upsertCandidateValue(candidate);
        assertNull(result);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void givenSnapshotAttrValue_whenUpsertedWithNewValueButSameSnapshotId_thenNotDuplicated() {
        BaseLastfmEntity entity = createDummyEntity();
        BaseLastfmEntity scopeEntity = createDummyEntity();
        LastfmApiCall apiCall = createDummyApiCall();
        LastfmAttributeHistoryRecord candidate = initAttrValueBuilder(entity, scopeEntity, apiCall)
                .attribute(SNAPSHOT_ATTRIBUTE)
                .intValue(42)
            .build();
        LastfmAttributeHistoryRecord saved = service.upsertCandidateValue(candidate);
        LastfmAttributeHistoryRecord newValueForSameSnapshot = initAttrValueBuilder(entity, scopeEntity, apiCall)
                .attribute(SNAPSHOT_ATTRIBUTE)
                .validFrom(candidate.getValidFrom().plusDays(1))
                .intValue(111)
            .build();

        LastfmAttributeHistoryRecord result = service.upsertCandidateValue(newValueForSameSnapshot);
        assertNull(result);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void givenSnapshotAttrValue_whenUpsertedTwiceWithTheDifferentSnapshotId_thenExpireOldAndSaveNew() {
        BaseLastfmEntity entity = createDummyEntity();
        BaseLastfmEntity scopeEntity = createDummyEntity();
        LastfmAttributeHistoryRecord candidate = initAttrValueBuilder(entity, scopeEntity, createDummyApiCall())
                .attribute(SNAPSHOT_ATTRIBUTE)
                .intValue(42)
            .build();
        service.upsertCandidateValue(candidate);

        LastfmAttributeHistoryRecord newValueForSameSnapshot = initAttrValueBuilder(entity, scopeEntity, createDummyApiCall())
                .attribute(candidate.getAttribute())
                .intValue(candidate.getIntValue())
                .validFrom(candidate.getValidFrom().plusDays(1))
            .build();
        LastfmAttributeHistoryRecord result = service.upsertCandidateValue(newValueForSameSnapshot);

        assertNotNull(result);

        List<LastfmAttributeHistoryRecord> all = repository.findAll();
        assertEquals(2, all.size());

        Optional<LastfmAttributeHistoryRecord> expired = all.stream()
                .filter(r -> !r.getValidTill().equals(LastfmConstants.END_OF_TIME))
            .findFirst();
        assertTrue(expired.isPresent());
        assertEquals(newValueForSameSnapshot.getValidFrom().minusDays(1), expired.get().getValidTill());
    }

    @Test
    void givenSCD2AttrValue_whenUpsertedNewValue_thenExpireOldAndSaveNew() {
        BaseLastfmEntity entity = createDummyEntity();
        BaseLastfmEntity scopeEntity = createDummyEntity();
        LastfmAttributeHistoryRecord candidate = initAttrValueBuilder(entity, scopeEntity, createDummyApiCall())
                .attribute(SCD2_ATTRIBUTE)
                .intValue(42)
            .build();
        LastfmAttributeHistoryRecord initial = service.upsertCandidateValue(candidate);
        LastfmAttributeHistoryRecord candidateUpdated = initAttrValueBuilder(entity, scopeEntity, createDummyApiCall())
                .attribute(candidate.getAttribute())
                .intValue(111)
                .validFrom(candidate.getValidFrom().plusDays(1))
            .build();
        LastfmAttributeHistoryRecord result = service.upsertCandidateValue(candidateUpdated);

        assertNotNull(result);

        List<LastfmAttributeHistoryRecord> all = repository.findAll();
        assertEquals(2, all.size());

        Optional<LastfmAttributeHistoryRecord> expired = all.stream()
                .filter(r -> !r.getValidTill().equals(LastfmConstants.END_OF_TIME))
            .findFirst();
        assertTrue(expired.isPresent());
        assertEquals(candidateUpdated.getValidFrom().minusDays(1), expired.get().getValidTill());
    }

    @Test
    void givenEmptyTable_whenUpsertCandidateValues_thenAllCandidatesAreProcessed() {
        LastfmAttributeHistoryRecord candidate1 = initAttrValueBuilder(createDummyEntity(), createDummyEntity(), createDummyApiCall())
                .attribute(SCD2_ATTRIBUTE)
                .intValue(42)
            .build();
        LastfmAttributeHistoryRecord candidate2 = initAttrValueBuilder(createDummyEntity(), createDummyEntity(), createDummyApiCall())
                .attribute(SNAPSHOT_ATTRIBUTE)
                .stringValue("http://example.org")
            .build();
        List<LastfmAttributeHistoryRecord> results = service.upsertCandidateValues(Arrays.asList(candidate1, candidate2));
        assertEquals(2, results.size());
        assertEquals(2, repository.findAll().size());
    }
}