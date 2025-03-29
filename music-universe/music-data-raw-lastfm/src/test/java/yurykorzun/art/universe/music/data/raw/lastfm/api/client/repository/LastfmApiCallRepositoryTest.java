package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LastfmApiCallRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmApiCallRepository repository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;


    @Test
    void testApiCallCreation() {
        LastfmApiCall created = LastfmApiCall.builder()
                .dataSnapshotId(consistencyHelper.createDummyDataSnapshot().getId())
                .type(LastfmApiCallType.TAG_TOP_TAGS)
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
    void testApiCallStatusUpdate() {
        LastfmApiCall created = LastfmApiCall.builder()
                .dataSnapshotId(consistencyHelper.createDummyDataSnapshot().getId())
                .type(LastfmApiCallType.TAG_TOP_TAGS)
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
}
