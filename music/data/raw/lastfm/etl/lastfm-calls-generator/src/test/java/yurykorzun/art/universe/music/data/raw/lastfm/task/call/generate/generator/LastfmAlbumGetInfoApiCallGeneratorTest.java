package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;

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
    private LastfmApiCallEntityService entityService;
    
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
