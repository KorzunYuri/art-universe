package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;
    @Mock
    private LastfmAlbumService albumService;

    @InjectMocks
    private LastfmAlbumGetInfoApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.ALBUM_GET_INFO, result);
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
    void selectEntitiesForApiCalls_shouldCallAlbumService() {
        // given
        List<LastfmAlbum> expectedAlbums = List.of(
            EntityCreationHelper.createAlbum(builder -> builder.name("Album 1")),
            EntityCreationHelper.createAlbum(builder -> builder.name("Album 2"))
        );
        when(albumService.findAlbumsForGetInfo()).thenReturn(expectedAlbums);

        // when
        List<LastfmAlbum> result = generator.selectEntitiesForApiCalls();

        // then
        assertEquals(expectedAlbums, result);
        verify(albumService).findAlbumsForGetInfo();
    }
}
