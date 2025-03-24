package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class LastfmDataSnapshotRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmDataSnapshotRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void givenNewDataSnapshot_whenPersisted_thenSavesCorrectValues() {
        //  given
        final Date dataDate = new Date();
        final int createdCnt = 3;
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate, createdCnt);

        //  when
        snapshot = repository.save(snapshot);
        snapshot = repository.findById(snapshot.getId()).get();

        //  then
        assertTrue(snapshot.getId() > 0);
        assertEquals(dataDate, snapshot.getDataDate());
        assertEquals(LastfmApiCallType.TAG_TOP_TAGS, snapshot.getApiCallType());
        assertEquals(createdCnt, snapshot.getCreatedCount());
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementCompletedCalled_thenIncrementsCorrectly() {
        //  given
        final Date dataDate = new Date();
        final int createdCnt = 3;
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate, createdCnt);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        //  when
        repository.incCompletedCount(id);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        //  then
        assertEquals(1, snapshot.getCompletedCount());
        assertEquals(0, snapshot.getParsedCount());
    }

    @Test
    void givenPersistedDataSnapshot_whenIncrementParsedCalled_thenIncrementsCorrectly() {
        // given
        final Date dataDate = new Date();
        final int createdCnt = 3;
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, dataDate, createdCnt);
        snapshot = repository.save(snapshot);
        final long id = snapshot.getId();

        // when
        repository.incParsedCount(id);
        entityManager.refresh(snapshot);
        snapshot = repository.findById(id).get();

        // then
        assertEquals(0, snapshot.getCompletedCount());
        assertEquals(1, snapshot.getParsedCount());
    }
}