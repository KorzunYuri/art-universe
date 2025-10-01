package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper.createAlbum;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumServiceImplTest {
    
    @Mock
    private LastfmAlbumRepository albumRepository;

    @InjectMocks
    private LastfmAlbumServiceImpl albumService;

    @Test
    void findAlbumsForGetInfo_shouldReturnRepositoryResult() {
        // given
        List<LastfmAlbum> expectedAlbums = List.of(
            createAlbum(),
            createAlbum()
        );
        when(albumRepository.findAlbumsForGetInfo()).thenReturn(expectedAlbums);

        // when
        List<LastfmAlbum> result = albumService.findAlbumsForGetInfo();

        // then
        assertEquals(expectedAlbums, result);
        verify(albumRepository).findAlbumsForGetInfo();
    }
}
