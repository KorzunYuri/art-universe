package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    
    @Mock
    private LastfmDataSnapshotService snapshotService;
    
    @Mock
    private LastfmEntityService entityService;
    
    @Mock
    private LastfmAlbumService albumService;
    
    @InjectMocks
    private LastfmAlbumGetInfoApiCallGenerator generator;
    
    @Test
    void getApiCallType_shouldReturnAlbumGetInfo() {
        // when
        LastfmApiCallType result = generator.getApiCallType();
        
        // then
        assertEquals(LastfmApiCallType.ALBUM_GET_INFO, result);
    }
    
    @Test
    void selectEntitiesForApiCalls_shouldUseAlbumService() {
        // given
        List<LastfmAlbum> expectedAlbums = List.of(
            EntityCreationHelper.createAlbum(),
            EntityCreationHelper.createAlbum(),
            EntityCreationHelper.createAlbum()
        );
        when(albumService.findAlbumsForGetInfo()).thenReturn(expectedAlbums);
        
        // when
        List<LastfmAlbum> result = generator.selectEntitiesForApiCalls();
        
        // then
        verify(albumService, times(1)).findAlbumsForGetInfo();
        assertEquals(expectedAlbums, result, "Should return albums from service");
    }
}
