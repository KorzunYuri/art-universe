package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
