package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTest;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LastfmApiCallRepositoryTest extends LastfmJpaTest {

    @Autowired
    private LastfmApiCallRepository repository;

    @Autowired
    private TestLastfmDataSnapshotRepository snapshotRepository;

    private static LastfmApiCallType DEFAULT_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_TAGS;

    @Test
    void save_shouldCreateApiCall_whenValidDataProvided() {
        LastfmApiCall created = LastfmApiCall.builder()
                .dataSnapshotId(createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE).getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now())
            .build();

        LastfmApiCall saved = repository.save(created);
        assertNotNull(saved);

        LastfmApiCall fetched = repository.getReferenceById(saved.getId());
        assertNotNull(fetched);
        assertEquals(created.getType(), fetched.getType());
        assertEquals(created.getParams(), fetched.getParams());
    }

    @Test
    void setStatus_shouldUpdateStatus_whenValidTransitionProvided() {
        LastfmApiCall created = LastfmApiCall.builder()
                .dataSnapshotId(createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE).getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now())
                .status(ApiCallStatus.CREATED)
            .build();

        LastfmApiCall saved = repository.save(created);
        saved.setStatus(ApiCallStatus.EXPIRED);
        repository.save(saved);

        LastfmApiCall updated = repository.getReferenceById(saved.getId());
        assertNotNull(updated);
        assertEquals(ApiCallStatus.EXPIRED, updated.getStatus());
    }

    @Test
    void findAllUnexpiredByStatus_shouldReturnFilteredCalls_whenValidStatusProvided() {
        // given
        LastfmDataSnapshot snapshot = createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE);
        
        LastfmApiCall pendingCall = LastfmApiCall.builder()
                .dataSnapshotId(snapshot.getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now().plusSeconds(3600))
                .status(ApiCallStatus.PENDING)
                .build();
        
        LastfmApiCall expiredCall = LastfmApiCall.builder()
                .dataSnapshotId(snapshot.getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now().minusSeconds(3600))
                .status(ApiCallStatus.PENDING)
                .build();

        repository.save(pendingCall);
        repository.save(expiredCall);

        // when
        var result = repository.findAllUnexpiredByStatus(ApiCallStatus.PENDING.getCode(), 10);

        // then
        assertEquals(1, result.size());
        assertEquals(pendingCall.getId(), result.get(0).getId());
    }

    @Test
    void findAllUnprocessedUnexpired_shouldReturnPendingCalls_whenCalled() {
        // given
        LastfmDataSnapshot snapshot = createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE);
        
        LastfmApiCall pendingCall = LastfmApiCall.builder()
                .dataSnapshotId(snapshot.getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now().plusSeconds(3600))
                .status(ApiCallStatus.PENDING)
                .build();

        repository.save(pendingCall);

        // when
        var result = repository.findAllUnprocessedUnexpired(10);

        // then
        assertEquals(1, result.size());
        assertEquals(ApiCallStatus.PENDING, result.get(0).getStatus());
    }

    private LastfmDataSnapshot createAndSaveDataSnapshot(LastfmApiCallType apiCallType) {
        return snapshotRepository.save(EntityCreationHelper.createDataSnapshot(apiCallType));
    }
}
