package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyApiCallServiceImplTest {

    @Mock
    private SpotifyApiCallRepository apiCallRepository;

    @InjectMocks
    private SpotifyApiCallServiceImpl service;

    private SpotifyApiCall buildCall() {
        return SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS))
            .spotifyId("3TVXtAsR1Inumwj472S9r4")
            .build();
    }

    private SpotifyApiCall buildProcessingCall() {
        return SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS))
            .spotifyId("3TVXtAsR1Inumwj472S9r4")
            .status(ApiCallStatus.PROCESSING)
            .build();
    }

    @Test
    void findAllCreatedUnexpired_shouldDelegateToRepository() {
        SpotifyApiCall call = buildCall();
        when(apiCallRepository.findAllCreatedUnexpired()).thenReturn(List.of(call));

        List<SpotifyApiCall> result = service.findAllCreatedUnexpired();

        assertThat(result).hasSize(1).containsExactly(call);
    }

    @Test
    void updateApiCallStatus_shouldSetStatusAndSave() {
        SpotifyApiCall call = buildProcessingCall();

        service.updateApiCallStatus(call, ApiCallStatus.SUCCESSFUL);

        assertThat(call.getStatus()).isEqualTo(ApiCallStatus.SUCCESSFUL);
        verify(apiCallRepository).save(call);
    }

    @Test
    void markForRetry_shouldSetDueToRetryStatusAndSave() {
        SpotifyApiCall call = buildProcessingCall();

        service.markForRetry(call);

        assertThat(call.getStatus()).isEqualTo(ApiCallStatus.DUE_TO_RETRY);
        verify(apiCallRepository).save(call);
    }

    @Test
    void finalizeApiCall_shouldSetAllFieldsAndSave() {
        SpotifyApiCall call = buildProcessingCall();

        service.finalizeApiCall(call, ApiCallStatus.SUCCESSFUL, 200, null);

        assertThat(call.getStatus()).isEqualTo(ApiCallStatus.SUCCESSFUL);
        assertThat(call.getHttpStatus()).isEqualTo(200);
        assertThat(call.getErrorMessage()).isNull();
        assertThat(call.getExecutedDttm()).isNotNull();
        verify(apiCallRepository).save(call);
    }

    @Test
    void finalizeApiCall_withError_shouldSetErrorFields() {
        SpotifyApiCall call = buildProcessingCall();

        service.finalizeApiCall(call, ApiCallStatus.FAILED, 429, "Rate limit exceeded");

        assertThat(call.getStatus()).isEqualTo(ApiCallStatus.FAILED);
        assertThat(call.getHttpStatus()).isEqualTo(429);
        assertThat(call.getErrorMessage()).isEqualTo("Rate limit exceeded");
        verify(apiCallRepository).save(call);
    }
}
