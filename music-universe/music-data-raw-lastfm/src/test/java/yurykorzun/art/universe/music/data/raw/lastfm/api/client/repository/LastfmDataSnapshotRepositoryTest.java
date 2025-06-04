package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmDataSnapshotRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmDataSnapshotRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Test
    void givenNewDataSnapshot_whenPersisted_thenSavesCorrectValues() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);

        //  when
        snapshot = repository.save(snapshot);
        snapshot = repository.findById(snapshot.getId()).get();

        //  then
        assertTrue(snapshot.getId() > 0);
        assertEquals(dataDate, snapshot.getDataDate());
        assertEquals(LastfmApiCallType.TAG_TOP_TAGS, snapshot.getApiCallType());
        assertEquals(0, snapshot.getCreatedCount());
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenExistingDataSnapshotForType_whenRequested_thenRetrieved() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_TAGS;
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(apiCallType, dataDate);

        //  when
        repository.save(snapshot);
        snapshot = repository.findForApiCallType(apiCallType);

        //  then
        assertNotNull(snapshot);
    }

    @Test
    void givenExistingDataSnapshotForEntity_whenRequested_thenRetrieved() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        BaseLastfmEntity entity = consistencyHelper.createAndSaveDummyEntity();

        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(apiCallType, dataDate, entity);

        //  when
        repository.save(snapshot);
        snapshot = repository.findForApiCallTypeAndEntity(apiCallType, entity);

        //  then
        assertNotNull(snapshot);
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementCreated_thenIncrementsCorrectly() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        //  when
        repository.incCreatedCount(id);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        //  then
        assertEquals(1, snapshot.getCreatedCount());
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementCompleted_thenIncrementsCorrectly() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        //  when
        repository.incCompletedCount(id);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        //  then
        assertEquals(0, snapshot.getCreatedCount());
        assertEquals(1, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementParsed_thenIncrementsCorrectly() {
        // given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        // when
        repository.incParsedCount(id);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        // then
        assertEquals(0, snapshot.getCreatedCount());
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(1, snapshot.getParsedCount());
    }


    @Test
    void givenPersistedDataSnapshot_whenIncrementCreatedByNumber_thenIncrementsCorrectly() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        //  when
        repository.incCreatedCountByNumber(id, 3);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        //  then
        assertEquals(3, snapshot.getCreatedCount());
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementCompletedByNumber_thenIncrementsCorrectly() {
        //  given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        //  when
        repository.incCompletedCountByNumber(id, 3);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        //  then
        assertEquals(0, snapshot.getCreatedCount());
        assertEquals(3, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementParsedByNumber_thenIncrementsCorrectly() {
        // given
        final LocalDate dataDate = LocalDate.now();
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        // when
        repository.incParsedCountByNumber(id, 3);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        // then
        assertEquals(0, snapshot.getCreatedCount());
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(3, snapshot.getParsedCount());
    }
}