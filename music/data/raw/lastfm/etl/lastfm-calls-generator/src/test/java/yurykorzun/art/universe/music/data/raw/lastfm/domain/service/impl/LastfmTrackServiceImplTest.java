package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTrackServiceImplTest {
    
    @Mock
    private LastfmTrackRepository trackRepository;

    @InjectMocks
    private LastfmTrackServiceImpl trackService;

    @Test
    void findTracksForGetInfo_shouldReturnRepositoryResult() {
        // given
        List<LastfmTrack> expectedTracks = List.of(
            EntityCreationHelper.createTrack(),
            EntityCreationHelper.createTrack()
        );
        when(trackRepository.findTracksForGetInfo()).thenReturn(expectedTracks);

        // when
        List<LastfmTrack> result = trackService.findTracksForGetInfo();

        // then
        assertEquals(expectedTracks, result);
        verify(trackRepository).findTracksForGetInfo();
    }
}
