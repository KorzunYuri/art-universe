package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistSearchApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmArtistSearchRequestService searchRequestService;

    @InjectMocks
    private LastfmArtistSearchApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.ARTIST_SEARCH, result);
    }

    @Test
    void createApiCalls_shouldCreateApiCallsAndMarkRequestsAsProcessed() {
        LastfmArtistSearchRequest request = mock(LastfmArtistSearchRequest.class);
        when(request.getSearchString()).thenReturn("Radiohead");
        when(searchRequestService.findUnprocessed(anyInt())).thenReturn(List.of(request));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.ARTIST_SEARCH);
        when(snapshotService.getOrCreateSnapshotFor(LastfmApiCallType.ARTIST_SEARCH)).thenReturn(snapshot);

        generator.createApiCalls();

        verify(apiCallService).createApiCalls(any(List.class));
        verify(request).setProcessed(true);
        verify(searchRequestService).saveRequests(List.of(request));
    }

    @Test
    void createApiCalls_shouldHandleEmptySearchRequests() {
        when(searchRequestService.findUnprocessed(anyInt())).thenReturn(List.of());
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.ARTIST_SEARCH);
        when(snapshotService.getOrCreateSnapshotFor(LastfmApiCallType.ARTIST_SEARCH)).thenReturn(snapshot);

        generator.createApiCalls();

        verify(apiCallService).createApiCalls(argThat(List::isEmpty));
    }
}
