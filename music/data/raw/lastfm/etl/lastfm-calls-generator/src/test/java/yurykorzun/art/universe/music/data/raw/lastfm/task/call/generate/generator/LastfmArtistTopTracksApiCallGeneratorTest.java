package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LastfmArtistTopTracksApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @InjectMocks
    private LastfmArtistTopTracksApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.ARTIST_TOP_TRACKS, result);
    }

    @Test
    void getDueDurationDays_shouldReturnConfiguredValue() {
        // given
        int expectedDays = 28;
        ReflectionTestUtils.setField(generator, "dueDurationDays", expectedDays);

        // when
        int result = generator.getDueDurationDays();

        // then
        assertEquals(expectedDays, result);
    }
}
