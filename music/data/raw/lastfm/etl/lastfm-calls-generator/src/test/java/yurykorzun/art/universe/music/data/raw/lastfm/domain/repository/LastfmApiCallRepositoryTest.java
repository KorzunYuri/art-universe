package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LastfmApiCallRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmApiCallRepository repository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    private static LastfmApiCallType DEFAULT_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_TAGS;

    @Test
    void saveAll_shouldSaveApiCalls_whenValidDataProvided() {
        LastfmApiCall call1 = LastfmApiCall.builder()
                .dataSnapshotId(createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE).getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value1"))
                .dueDttm(Instant.now())
                .build();

        LastfmApiCall call2 = LastfmApiCall.builder()
                .dataSnapshotId(createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE).getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "value2"))
                .dueDttm(Instant.now())
                .build();

        List<LastfmApiCall> saved = repository.saveAll(List.of(call1, call2));
        
        assertEquals(2, saved.size());
        assertNotNull(saved.get(0).getId());
        assertNotNull(saved.get(1).getId());
    }

    @Test
    void findAllUnexpiredByType_shouldReturnUnexpiredCalls() {
        LastfmApiCall expiredCall = LastfmApiCall.builder()
                .dataSnapshotId(createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE).getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "expired"))
                .dueDttm(Instant.now().minusSeconds(3600))
                .build();

        LastfmApiCall unexpiredCall = LastfmApiCall.builder()
                .dataSnapshotId(createAndSaveDataSnapshot(DEFAULT_API_CALL_TYPE).getId())
                .type(DEFAULT_API_CALL_TYPE)
                .params(Map.of("key", "unexpired"))
                .dueDttm(Instant.now().plusSeconds(3600))
                .build();

        repository.saveAll(List.of(expiredCall, unexpiredCall));

        List<LastfmApiCall> result = repository.findAllUnexpiredByType(DEFAULT_API_CALL_TYPE);
        
        assertEquals(1, result.size());
        assertEquals("unexpired", result.get(0).getParams().get("key"));
    }

    private LastfmDataSnapshot createAndSaveDataSnapshot(LastfmApiCallType apiCallType) {
        return snapshotRepository.save(EntityCreationHelper.createDataSnapshot(apiCallType));
    }
}
