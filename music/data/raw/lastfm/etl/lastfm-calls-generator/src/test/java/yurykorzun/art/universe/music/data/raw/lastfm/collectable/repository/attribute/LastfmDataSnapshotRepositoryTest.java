package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LastfmDataSnapshotRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmDataSnapshotRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Test
    void save_shouldSaveCorrectValues_whenNewDataSnapshotPersisted() {
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
    void findByApiCallType_shouldRetrieveSnapshot_whenExistingDataSnapshotForTypeRequested() {
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
    void findByApiCallTypeAndEntityTypeAndEntityId_shouldRetrieveSnapshot_whenExistingDataSnapshotForEntityRequested() {
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
    void incrementCreated_shouldIncrementCorrectly_whenPersistedDataSnapshotUpdated() {
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
    void incrementCreatedByNumber_shouldIncrementCorrectly_whenPersistedDataSnapshotUpdated() {
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
}