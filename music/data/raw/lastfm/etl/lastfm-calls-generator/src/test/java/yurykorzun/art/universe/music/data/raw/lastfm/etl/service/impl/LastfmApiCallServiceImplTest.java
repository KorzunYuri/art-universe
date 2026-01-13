package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallServiceImplTest {

    @Mock
    private LastfmApiCallRepository apiCallRepository;

    @InjectMocks
    private LastfmApiCallServiceImpl service;

    @Test
    void createApiCalls_shouldCreateAndReturnIds_whenValidRequestsProvided() {
        // given
        LastfmApiCallCreateRequest request = LastfmApiCallCreateRequest.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .entityType(LastfmEntityType.TAG)
                .entityId(1L)
                .dataSnapshotId(1L)
                .dueDttm(Instant.now())
                .params(Map.of("tag", "rock"))
                .build();

        LastfmApiCall savedCall = LastfmApiCall.builder()
                .id(123L)
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .status(ApiCallStatus.PENDING)
                .dueDttm(Instant.now())
                .build();

        when(apiCallRepository.saveAll(any())).thenReturn(List.of(savedCall));

        // when
        List<Long> result = service.createApiCalls(List.of(request));

        // then
        assertEquals(List.of(123L), result);
        verify(apiCallRepository).saveAll(argThat(calls -> {
            List<LastfmApiCall> callsList = (List<LastfmApiCall>) calls;
            return callsList.size() == 1 && 
                   callsList.getFirst().getStatus() == ApiCallStatus.PENDING &&
                   callsList.getFirst().getType() == LastfmApiCallType.TAG_TOP_TAGS;
        }));
    }

    @Test
    void findAllUnexpiredByType_shouldReturnRepositoryResult() {
        // given
        LastfmApiCallType type = LastfmApiCallType.ARTIST_GET_INFO;
        List<LastfmApiCall> expectedCalls = List.of(EntityCreationHelper.createApiCall());
        when(apiCallRepository.findAllUnexpiredByType(type)).thenReturn(expectedCalls);

        // when
        List<LastfmApiCall> result = service.findAllUnexpiredByType(type);

        // then
        assertEquals(expectedCalls, result);
        verify(apiCallRepository).findAllUnexpiredByType(type);
    }
}
