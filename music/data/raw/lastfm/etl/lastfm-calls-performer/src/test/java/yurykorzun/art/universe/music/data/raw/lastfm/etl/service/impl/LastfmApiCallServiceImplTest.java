package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallServiceImplTest {

    @Mock
    private LastfmApiCallRepository apiCallRepository;

    @InjectMocks
    private LastfmApiCallServiceImpl service;

    @Test
    void findAllUnprocessedUnexpired_shouldDelegateToRepository() {
        LastfmApiCall call = EntityCreationHelper.createApiCall();
        when(apiCallRepository.findAllUnprocessedUnexpired()).thenReturn(List.of(call));

        List<LastfmApiCall> result = service.findAllUnprocessedUnexpired();

        assertEquals(List.of(call), result);
        verify(apiCallRepository).findAllUnprocessedUnexpired();
    }

    @Test
    void updateApiCallStatus_shouldSetStatusAndSave() {
        LastfmApiCall call = EntityCreationHelper.createApiCall();

        service.updateApiCallStatus(call, ApiCallStatus.PROCESSING);

        verify(apiCallRepository).save(call);
        assertEquals(ApiCallStatus.PROCESSING, call.getStatus());
    }
}
