package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.TestBeansConfig;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseServiceImplTest {

    @Mock
    private LastfmApiResponseRepository apiResponseRepository;

    private LastfmApiResponseServiceImpl apiResponseService;

    @BeforeEach
    void setUp() {
        apiResponseService = new LastfmApiResponseServiceImpl(
            apiResponseRepository,
            TestBeansConfig.getObjectMapper()
        );
    }

    private static final String sampleResponse = "{}";

    private Supplier<LastfmApiResponseCreateRequest> validCreateResponseRequestSupplier() {
        return () -> LastfmApiResponseCreateRequest.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .responseBody(sampleResponse)
                .build();
    }

    private static LastfmApiResponse getMockResponse(LastfmApiResponseCreateRequest request, long id) {
        return LastfmApiResponse.builder()
                .id(id)
                .apiCall(request.getApiCall())
                .responseBody(request.getResponseBody())
            .build();
    }

    @Test
    void createResponse_shouldCreateApiResponse_whenValidDataProvided() {
        // given
        long id = 1L;
        LastfmApiResponseCreateRequest request = validCreateResponseRequestSupplier().get();
        LastfmApiResponse created = getMockResponse(request, id);
        when(apiResponseRepository.save(any(LastfmApiResponse.class))).thenReturn(created);

        // when
        long returnedId = apiResponseService.createResponse(request);

        // then
        verify(apiResponseRepository).save(any(LastfmApiResponse.class));
        assertEquals(id, returnedId);
    }

}
