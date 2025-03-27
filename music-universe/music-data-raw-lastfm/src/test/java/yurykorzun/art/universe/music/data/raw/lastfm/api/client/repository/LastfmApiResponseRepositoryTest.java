package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


class LastfmApiResponseRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmApiResponseRepository apiResponseRepository;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();
    private static final LastfmApiCallType dummyApiCallType = LastfmApiCallType.TAG_TOP_TAGS;

    private LastfmDataSnapshot createDummyDataSnapshot() {
        return snapshotRepository.save(new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, new Date()));
    }

    private LastfmApiCall createDummyApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot();
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(dummyApiCallType)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
            .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    private LastfmApiResponse responseForApiCall(LastfmApiCall apiCall) {
        return LastfmApiResponse.builder()
                .apiCall(apiCall)
                .responseBody(sampleResponse)
            .build();
    }

    @Test
    void testApiResponseCreation() {
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = apiResponseRepository.save(created);

        assertNotNull(saved);
        assertEquals(dummyApiCall, saved.getApiCall());
        assertEquals(sampleResponse, saved.getResponseBody());
    }

    @Test
    void testApiResponseStatusUpdate() {

        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = apiResponseRepository.save(created);

        ApiResponseStatus newStatus = ApiResponseStatus.PENDING;
        saved.setStatus(newStatus);
        apiResponseRepository.save(created);

        LastfmApiResponse updated = apiResponseRepository.getReferenceById(saved.getId());
        assertNotNull(updated);
        assertEquals(newStatus, updated.getStatus());
    }

    @Test
    void testApiResponseDuplicationPrevention() {
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = apiResponseRepository.save(created);

        LastfmApiResponse duplicate = responseForApiCall(dummyApiCall);
        LastfmApiResponse duplicateSaved = apiResponseRepository.save(created);

        long count = apiResponseRepository.count();
        assertEquals(1, count);

        // TODO when there is more than one ApiCallType - check that response for another ApiCallType doesn't count as a duplicate
    }

}