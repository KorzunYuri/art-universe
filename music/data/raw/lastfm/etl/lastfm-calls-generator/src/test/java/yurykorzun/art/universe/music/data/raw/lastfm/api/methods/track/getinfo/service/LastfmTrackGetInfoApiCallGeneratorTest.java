package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTrackGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;
    @Mock
    private LastfmTrackService trackService;

    @InjectMocks
    private LastfmTrackGetInfoApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.TRACK_GET_INFO, result);
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

    @Test
    void selectEntitiesForApiCalls_shouldCallTrackService() {
        // given
        List<LastfmTrack> expectedTracks = List.of(
            EntityCreationHelper.createTrack(builder -> builder.name("Track 1")),
            EntityCreationHelper.createTrack(builder -> builder.name("Track 2"))
        );
        when(trackService.findTracksForGetInfo()).thenReturn(expectedTracks);

        // when
        List<LastfmTrack> result = generator.selectEntitiesForApiCalls();

        // then
        assertEquals(expectedTracks, result);
        verify(trackService).findTracksForGetInfo();
    }
}
