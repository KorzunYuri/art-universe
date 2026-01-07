package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.entity.Track;
import yurykorzun.art.universe.music.quiz.repository.TrackRepository;
import yurykorzun.art.universe.music.quiz.service.impl.TrackServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @InjectMocks
    private TrackServiceImpl trackService;

    @Test
    void bind_shouldReturnExistingBinding_whenTrackAlreadyBound() {
        // given
        Long masterId = 1L;
        Track existingTrack = new Track();
        existingTrack.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Track.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingTrack, 100L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(trackRepository.findByMasterId(masterId)).thenReturn(Optional.of(existingTrack));

        // when
        BindingDto result = trackService.bind(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(100L, result.getBindingId());
        verify(trackRepository).findByMasterId(masterId);
        verify(trackRepository, never()).save(any());
    }

    @Test
    void bind_shouldCreateNewBinding_whenTrackNotBound() {
        // given
        Long masterId = 2L;
        Track savedTrack = new Track();
        savedTrack.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Track.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedTrack, 200L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(trackRepository.findByMasterId(masterId)).thenReturn(Optional.empty());
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        // when
        BindingDto result = trackService.bind(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(200L, result.getBindingId());
        verify(trackRepository).findByMasterId(masterId);
        verify(trackRepository).save(any(Track.class));
    }

    @Test
    void bind_shouldHandleRaceCondition_whenDataIntegrityViolationOccurs() {
        // given
        Long masterId = 3L;
        Track existingTrack = new Track();
        existingTrack.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Track.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingTrack, 300L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(trackRepository.findByMasterId(masterId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingTrack));
        when(trackRepository.save(any(Track.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // when
        BindingDto result = trackService.bind(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(300L, result.getBindingId());
        verify(trackRepository, times(2)).findByMasterId(masterId);
        verify(trackRepository).save(any(Track.class));
    }

    @Test
    void unbind_shouldRemoveBinding_whenTrackExists() {
        // given
        Long masterId = 4L;
        Track existingTrack = new Track();
        existingTrack.setMasterId(masterId);

        when(trackRepository.findByMasterId(masterId)).thenReturn(Optional.of(existingTrack));
        doNothing().when(trackRepository).deleteByMasterId(masterId);

        // when
        BindingDto result = trackService.unbind(masterId);

        // then
        assertFalse(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertNull(result.getBindingId());
        verify(trackRepository).findByMasterId(masterId);
        verify(trackRepository).deleteByMasterId(masterId);
    }

    @Test
    void unbind_shouldReturnUnboundResult_whenTrackNotExists() {
        // given
        Long masterId = 5L;

        when(trackRepository.findByMasterId(masterId)).thenReturn(Optional.empty());

        // when
        BindingDto result = trackService.unbind(masterId);

        // then
        assertFalse(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertNull(result.getBindingId());
        verify(trackRepository).findByMasterId(masterId);
        verify(trackRepository, never()).deleteByMasterId(any());
    }

    @Test
    void getBinding_shouldReturnBoundResult_whenTrackExists() {
        // given
        Long masterId = 6L;
        Track existingTrack = new Track();
        existingTrack.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Track.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingTrack, 600L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(trackRepository.findByMasterId(masterId)).thenReturn(Optional.of(existingTrack));

        // when
        BindingDto result = trackService.getBinding(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(600L, result.getBindingId());
        verify(trackRepository).findByMasterId(masterId);
    }

    @Test
    void getBinding_shouldReturnUnboundResult_whenTrackNotExists() {
        // given
        Long masterId = 7L;

        when(trackRepository.findByMasterId(masterId)).thenReturn(Optional.empty());

        // when
        BindingDto result = trackService.getBinding(masterId);

        // then
        assertFalse(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertNull(result.getBindingId());
        verify(trackRepository).findByMasterId(masterId);
    }

    @Test
    void getBindings_shouldReturnMixedResults_whenSomeTracksExist() {
        // given
        List<Long> masterIds = List.of(10L, 20L, 30L);
        
        Track track1 = new Track();
        track1.setMasterId(10L);
        Track track3 = new Track();
        track3.setMasterId(30L);
        
        // Use reflection to set ids since setter is private
        try {
            var idField = Track.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(track1, 1000L);
            idField.set(track3, 3000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Track> boundTracks = List.of(track1, track3);
        when(trackRepository.findByMasterIdIn(masterIds)).thenReturn(boundTracks);

        // when
        List<BindingDto> results = trackService.getBindings(masterIds);

        // then
        assertEquals(3, results.size());
        
        BindingDto result1 = results.get(0);
        assertTrue(result1.isBound());
        assertEquals(10L, result1.getMasterId());
        assertEquals(1000L, result1.getBindingId());
        
        BindingDto result2 = results.get(1);
        assertFalse(result2.isBound());
        assertEquals(20L, result2.getMasterId());
        assertNull(result2.getBindingId());
        
        BindingDto result3 = results.get(2);
        assertTrue(result3.isBound());
        assertEquals(30L, result3.getMasterId());
        assertEquals(3000L, result3.getBindingId());
        
        verify(trackRepository).findByMasterIdIn(masterIds);
    }

    @Test
    void getBindings_shouldReturnAllUnboundResults_whenNoTracksExist() {
        // given
        List<Long> masterIds = List.of(100L, 200L);

        when(trackRepository.findByMasterIdIn(masterIds)).thenReturn(List.of());

        // when
        List<BindingDto> results = trackService.getBindings(masterIds);

        // then
        assertEquals(2, results.size());
        
        results.forEach(result -> {
            assertFalse(result.isBound());
            assertNull(result.getBindingId());
            assertTrue(masterIds.contains(result.getMasterId()));
        });
        
        verify(trackRepository).findByMasterIdIn(masterIds);
    }
}
