package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistSearchRequestRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistSearchRequestServiceImplTest {
    
    @Mock
    private LastfmArtistSearchRequestRepository searchRequestRepository;

    @InjectMocks
    private LastfmArtistSearchRequestServiceImpl service;

    @Test
    void findUnprocessed_shouldReturnRepositoryResult() {
        // given
        int batchLimit = 10;
        List<LastfmArtistSearchRequest> expectedRequests = List.of(
            LastfmArtistSearchRequest.builder().id(1L).searchString("Artist 1").build(),
            LastfmArtistSearchRequest.builder().id(2L).searchString("Artist 2").build()
        );
        when(searchRequestRepository.findUnprocessed(batchLimit)).thenReturn(expectedRequests);

        // when
        List<LastfmArtistSearchRequest> result = service.findUnprocessed(batchLimit);

        // then
        assertEquals(expectedRequests, result);
        verify(searchRequestRepository).findUnprocessed(batchLimit);
    }

    @Test
    void saveRequests_shouldReturnRepositoryResult() {
        // given
        List<LastfmArtistSearchRequest> requests = List.of(
            LastfmArtistSearchRequest.builder().searchString("Artist 1").build(),
            LastfmArtistSearchRequest.builder().searchString("Artist 2").build()
        );
        List<LastfmArtistSearchRequest> savedRequests = List.of(
            LastfmArtistSearchRequest.builder().id(1L).searchString("Artist 1").build(),
            LastfmArtistSearchRequest.builder().id(2L).searchString("Artist 2").build()
        );
        when(searchRequestRepository.saveAll(requests)).thenReturn(savedRequests);

        // when
        List<LastfmArtistSearchRequest> result = service.saveRequests(requests);

        // then
        assertEquals(savedRequests, result);
        verify(searchRequestRepository).saveAll(requests);
    }
}
