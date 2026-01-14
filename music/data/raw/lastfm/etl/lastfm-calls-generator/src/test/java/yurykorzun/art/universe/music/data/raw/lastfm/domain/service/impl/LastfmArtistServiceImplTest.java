package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistServiceImplTest {
    
    @Mock
    private LastfmArtistRepository artistRepository;

    @InjectMocks
    private LastfmArtistServiceImpl artistService;

    @Test
    void findArtistsForGetInfo_shouldReturnRepositoryResult() {
        // given
        List<LastfmArtist> expectedArtists = List.of(
            EntityCreationHelper.createArtist(),
            EntityCreationHelper.createArtist()
        );
        when(artistRepository.findAllToGetInfoFor()).thenReturn(expectedArtists);

        // when
        List<LastfmArtist> result = artistService.findArtistsForGetInfo();

        // then
        assertEquals(expectedArtists, result);
        verify(artistRepository).findAllToGetInfoFor();
    }
}
