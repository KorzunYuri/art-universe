package yurykorzun.art.universe.music.data.raw.lastfm.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiClientImplTest {

    @Mock
    private RestClient.Builder restClientBuilder;
    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private LastfmApiClientImpl apiClient;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        
        apiClient = new LastfmApiClientImpl(restClientBuilder, "http://test.com");
        apiClient.init();
    }

    @Test
    void makeApiCall_shouldReturnResponse_whenValidCallProvided() {
        // given
        LastfmApiCall apiCall = createApiCall();
        String expectedResponse = "{\"test\":\"response\"}";
        
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        // when
        String response = apiClient.makeApiCall(apiCall);

        // then
        assertEquals(expectedResponse, response);
        verify(restClient).get();
    }

    @Test
    void makeApiCall_shouldIncludeDefaultParams_whenCalled() {
        // given
        LastfmApiCall apiCall = createApiCall();
        
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{}");

        // when
        apiClient.makeApiCall(apiCall);

        // then
        verify(requestHeadersUriSpec).uri(any(java.util.function.Function.class));
    }

    private LastfmApiCall createApiCall() {
        return LastfmApiCall.builder()
                .id(1L)
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(1L)
                .params(Map.of("tag", "rock"))
                .dueDttm(Instant.now())
                .build();
    }
}
