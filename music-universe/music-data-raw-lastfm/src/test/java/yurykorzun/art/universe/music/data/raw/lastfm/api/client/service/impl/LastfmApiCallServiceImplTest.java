package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class LastfmApiCallServiceImplTest {

    @MockitoBean
    private LastfmApiCallRepository repository;

    @Autowired
    private LastfmApiCallServiceImpl service;

    private static final Instant dueDttm = Instant.now().plus(Duration.ofDays(1));
    private static final Supplier<LastfmApiCallCreateRequest> validCreateRequestSupplier =
            () -> LastfmApiCallCreateRequest.builder()
                        .type(LastfmApiCallType.TAG_TOP_TAGS)
                        .dueDttm(dueDttm)
                    .build();

    @Test
    void testApiCallCreation() {
        // given
        LastfmApiCallCreateRequest request = validCreateRequestSupplier.get();
        LastfmApiCall apiCall = LastfmApiCall.builder()
                .id(1L)
                .dueDttm(request.getDueDttm())
                .type(request.getType())
            .build();
        when(repository.save(any(LastfmApiCall.class))).thenReturn(apiCall);

        // when
        long returnedId = service.createApiCall(request);

        // then
        verify(repository).save(any(LastfmApiCall.class));
        assertEquals(1L, returnedId);
    }

    @Test
    void testApiCallStatusUpdate() {
        // given
        long id = 1L;
        LastfmApiCall apiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dueDttm(Instant.now())
            .build();
        when(repository.getReferenceById(id)).thenReturn(apiCall);

        // when
        service.setStatus(id, ApiCallStatus.PENDING);

        // then
        verify(repository).getReferenceById(id);
        verify(repository).save(apiCall);
        assertEquals(ApiCallStatus.PENDING, apiCall.getStatus());
    }
}