package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
class LastfmApiResponseRepositoryTest {

    @Autowired
    private LastfmApiResponseRepository repository;

    @Autowired
    private LastfmApiCallRepository lastfmApiCallRepository;

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();
    private static final LastfmApiCallType dummyApiCallType = LastfmApiCallType.TAG_TOP_TAGS;

    private LastfmApiCall createDummyApiCall() {
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(dummyApiCallType)
                .dueDttm(Instant.now())
            .build();
        dummyApiCall = lastfmApiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    private LastfmApiResponse responseForApiCall(LastfmApiCall apiCall) {
        return LastfmApiResponse.builder()
                .apiCallId(apiCall.getId())
                .apiCallType(apiCall.getType())
                .responseBody(sampleResponse)
            .build();
    }

    @Test
    void testApiResponseCreation() {
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = repository.save(created);

        assertNotNull(saved);
        assertEquals(dummyApiCall.getId(), saved.getApiCallId());
        assertEquals(dummyApiCall.getType(), saved.getApiCallType());
        assertEquals(sampleResponse, saved.getResponseBody());
    }

    @Test
    void testApiResponseStatusUpdate() {

        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = repository.save(created);

        ApiResponseStatus newStatus = ApiResponseStatus.PENDING;
        saved.setStatus(newStatus);
        repository.save(created);

        LastfmApiResponse updated = repository.getReferenceById(saved.getId());
        assertNotNull(updated);
        assertEquals(newStatus, updated.getStatus());
    }

    @Test
    void testApiResponseFKApiCallConstraint() {
        LastfmApiResponse orphanResponse = LastfmApiResponse.builder()
                .apiCallId(2L)
                .apiCallType(dummyApiCallType)
                .responseBody(sampleResponse)
            .build();
        Exception e = assertThrows(Exception.class, () -> repository.saveAndFlush(orphanResponse));
        assertInstanceOf(DataIntegrityViolationException.class, e);
    }

    @Test
    void testApiResponseDuplicationPrevention() {
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = repository.save(created);

        LastfmApiResponse duplicate = responseForApiCall(dummyApiCall);
        LastfmApiResponse duplicateSaved = repository.save(created);

        long count = repository.count();
        assertEquals(1, count);

        // TODO when there is more than one ApiCallType - check that response for another ApiCallType doesn't count as a duplicate
    }

}