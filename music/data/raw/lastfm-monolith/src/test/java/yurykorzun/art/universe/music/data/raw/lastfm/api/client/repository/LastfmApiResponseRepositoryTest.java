package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmApiResponseRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmApiResponseRepository apiResponseRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();

    private LastfmApiResponse responseForApiCall(LastfmApiCall apiCall) {
        return LastfmApiResponse.builder()
                .apiCall(apiCall)
                .responseBody(sampleResponse)
            .build();
    }

    @Test
    void save_shouldCreateApiResponse_whenValidDataProvided() {
        LastfmApiCall dummyApiCall = consistencyHelper.createAndSaveApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = apiResponseRepository.save(created);

        assertNotNull(saved);
        assertEquals(dummyApiCall, saved.getApiCall());
        assertEquals(sampleResponse, saved.getResponseBody());
    }

    @Test
    void setStatus_shouldUpdateStatus_whenValidTransitionProvided() {

        LastfmApiCall dummyApiCall = consistencyHelper.createAndSaveApiCall();
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
    void save_shouldPreventDuplication_whenSameApiCallProvided() {
        LastfmApiCall dummyApiCall = consistencyHelper.createAndSaveApiCall();
        LastfmApiResponse created = responseForApiCall(dummyApiCall);
        LastfmApiResponse saved = apiResponseRepository.save(created);

        LastfmApiResponse duplicate = responseForApiCall(dummyApiCall);
        LastfmApiResponse duplicateSaved = apiResponseRepository.save(created);

        long count = apiResponseRepository.count();
        assertEquals(1, count);

        // TODO when there is more than one ApiCallType - check that response for another ApiCallType doesn't count as a duplicate
    }

}