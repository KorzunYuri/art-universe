package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LastfmTagTopTracksApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @InjectMocks
    private LastfmTagTopTracksApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.TAG_TOP_TRACKS, result);
    }

    @Test
    void getDueDurationDays_shouldReturnConfiguredValue() {
        // given
        int expectedDays = 7;
        ReflectionTestUtils.setField(generator, "dueDurationDays", expectedDays);

        // when
        int result = generator.getDueDurationDays();

        // then
        assertEquals(expectedDays, result);
    }
}
