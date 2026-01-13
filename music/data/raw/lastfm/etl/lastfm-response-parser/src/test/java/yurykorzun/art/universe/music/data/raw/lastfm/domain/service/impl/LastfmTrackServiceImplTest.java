package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTrackServiceImplTest {
    
    @Mock
    private LastfmTrackRepository trackRepository;

    @InjectMocks
    private LastfmTrackServiceImpl trackService;

    private LastfmTrack createTrack() {
        return EntityCreationHelper.createTrack();
    }

    private LastfmTrack createTrack(Consumer<LastfmTrack.LastfmTrackBuilder<?,?>> customizer) {
        return EntityCreationHelper.createTrack(customizer);
    }

    @Test
    void findById_shouldReturnTrackWhenExists() {
        // Given
        long trackId = 42L;
        LastfmTrack expectedTrack = createTrack(b -> b.id(trackId));
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(expectedTrack));

        // When
        Optional<LastfmTrack> result = trackService.findById(trackId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedTrack, result.get());
        verify(trackRepository).findById(trackId);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenTrackDoesNotExist() {
        // Given
        long trackId = 999L;
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // When
        Optional<LastfmTrack> result = trackService.findById(trackId);

        // Then
        assertFalse(result.isPresent());
        verify(trackRepository).findById(trackId);
    }

    @Test
    void saveAll_withValidTracks_shouldCallRepository() {
        List<LastfmTrack> tracks = List.of(createTrack(), createTrack());
        when(trackRepository.saveAll(tracks)).thenReturn(tracks);

        List<LastfmTrack> savedTracks = trackService.saveAll(tracks);

        assertNotNull(savedTracks);
        assertEquals(tracks.size(), savedTracks.size());
        assertEquals(tracks, savedTracks);
        verify(trackRepository, times(1)).saveAll(tracks);
    }
}
