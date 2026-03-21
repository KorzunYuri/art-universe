package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingIterationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyApiResponseProcessingOrchestratorTest {

    @Mock private SpotifyApiResponseRepository apiResponseRepository;
    @Mock private StagingIterationService stagingIterationService;
    @Mock private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SpotifyApiResponseProcessingOrchestrator orchestrator;

    @Test
    void processResponses_shouldReturnEarly_whenNoPendingResponses() {
        when(apiResponseRepository.findAllPending()).thenReturn(List.of());

        orchestrator.processResponses();

        verify(apiResponseRepository).findAllPending();
        verifyNoMoreInteractions(apiResponseRepository, stagingIterationService);
    }

    @Test
    void processSingle_shouldSetProcessingError_whenParsingIsDisabled() {
        SpotifyApiCall apiCall = mock(SpotifyApiCall.class);
        when(apiCall.getType()).thenReturn(SpotifyApiCallType.ARTIST_GET);

        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        when(response.getApiCall()).thenReturn(apiCall);

        StagingIteration iteration = mock(StagingIteration.class);

        // Parsing is disabled for this call type
        when(configPropertyHolder.getBoolean(any())).thenReturn(false);

        orchestrator.processSingle(response, iteration);

        verify(response).setStatus(ApiResponseStatus.PROCESSING_ERROR);
        verify(apiResponseRepository).save(response);
        verify(response, never()).setStatus(ApiResponseStatus.PROCESSING);
    }

    @Test
    void processSingle_shouldSetProcessingError_whenNoProcessorRegisteredForCallType() {
        // Use a call type that won't be registered in the static registry during this unit test
        // (no Spring context → CLASS_REGISTRY is empty for this type)
        SpotifyApiCall apiCall = mock(SpotifyApiCall.class);
        when(apiCall.getType()).thenReturn(SpotifyApiCallType.ARTIST_GET);

        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        when(response.getApiCall()).thenReturn(apiCall);

        StagingIteration iteration = mock(StagingIteration.class);

        // Parsing enabled but registry has no processor (no Spring context in unit test)
        when(configPropertyHolder.getBoolean(any())).thenReturn(true);

        orchestrator.processSingle(response, iteration);

        // processSingle sets PROCESSING_ERROR when processor == null
        verify(response).setStatus(ApiResponseStatus.PROCESSING_ERROR);
        verify(apiResponseRepository).save(response);
    }

    @Test
    void processResponses_shouldNotIncrementStaged_whenNoRecordsProcessed() {
        SpotifyApiCall apiCall = mock(SpotifyApiCall.class);
        when(apiCall.getType()).thenReturn(SpotifyApiCallType.ARTIST_GET);

        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        when(response.getApiCall()).thenReturn(apiCall);

        StagingIteration iteration = mock(StagingIteration.class);
        when(stagingIterationService.getOrCreateOpenIteration()).thenReturn(iteration);
        when(apiResponseRepository.findAllPending()).thenReturn(List.of(response));
        when(configPropertyHolder.getBoolean(any())).thenReturn(false); // disabled → staged=0

        orchestrator.processResponses();

        verify(stagingIterationService, never()).incrementRecordsStaged(any(), anyInt());
    }
}
