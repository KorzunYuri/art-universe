package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

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
            null
        );

        // spy, for self-injection to refer to the spied object
        apiResponseService = Mockito.spy(apiResponseService);

        // inject self
        ReflectionTestUtils.setField(apiResponseService, "self", apiResponseService);
    }

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();

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
    void testApiCallCreation() {
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

    @Test
    void testApiCallStatusUpdate() {
        // given
        long id = 1L;
        LastfmApiResponse apiResponse = getMockResponse(validCreateResponseRequestSupplier().get(), id);
        when(apiResponseRepository.getReferenceById(id)).thenReturn(apiResponse);

        // when
        apiResponseService.setStatus(id, ApiResponseStatus.PENDING);

        // then
        verify(apiResponseRepository).getReferenceById(id);
        verify(apiResponseRepository).save(apiResponse);
        assertEquals(ApiResponseStatus.PENDING, apiResponse.getStatus());
    }

}