package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmArtistService artistService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @InjectMocks
    private LastfmArtistGetInfoApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.ARTIST_GET_INFO, result);
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

    @Test
    void selectEntitiesForApiCalls_shouldCallArtistService() {
        // given
        List<LastfmArtist> expectedArtists = List.of(
            EntityCreationHelper.createArtist(builder -> builder.name("Artist 1")),
            EntityCreationHelper.createArtist(builder -> builder.name("Artist 2"))
        );
        when(artistService.findArtistsForGetInfo()).thenReturn(expectedArtists);

        // when
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

        // then
        assertEquals(expectedArtists, result);
        verify(artistService).findArtistsForGetInfo();
    }
}
