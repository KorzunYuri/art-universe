package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.entity.Artist;
import yurykorzun.art.universe.music.quiz.repository.ArtistRepository;
import yurykorzun.art.universe.music.quiz.service.impl.ArtistServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    void bind_shouldReturnExistingBinding_whenArtistAlreadyBound() {
        // given
        Long masterId = 1L;
        Artist existingArtist = new Artist();
        existingArtist.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Artist.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingArtist, 100L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(artistRepository.findByMasterId(masterId)).thenReturn(Optional.of(existingArtist));

        // when
        BindingDto result = artistService.bind(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(100L, result.getBindingId());
        verify(artistRepository).findByMasterId(masterId);
        verify(artistRepository, never()).save(any());
    }

    @Test
    void bind_shouldCreateNewBinding_whenArtistNotBound() {
        // given
        Long masterId = 2L;
        Artist savedArtist = new Artist();
        savedArtist.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Artist.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedArtist, 200L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(artistRepository.findByMasterId(masterId)).thenReturn(Optional.empty());
        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtist);

        // when
        BindingDto result = artistService.bind(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(200L, result.getBindingId());
        verify(artistRepository).findByMasterId(masterId);
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void bind_shouldHandleRaceCondition_whenDataIntegrityViolationOccurs() {
        // given
        Long masterId = 3L;
        Artist existingArtist = new Artist();
        existingArtist.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Artist.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingArtist, 300L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(artistRepository.findByMasterId(masterId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // when
        BindingDto result = artistService.bind(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(300L, result.getBindingId());
        verify(artistRepository, times(2)).findByMasterId(masterId);
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void unbind_shouldRemoveBinding_whenArtistExists() {
        // given
        Long masterId = 4L;
        Artist existingArtist = new Artist();
        existingArtist.setMasterId(masterId);

        when(artistRepository.findByMasterId(masterId)).thenReturn(Optional.of(existingArtist));
        doNothing().when(artistRepository).deleteByMasterId(masterId);

        // when
        BindingDto result = artistService.unbind(masterId);

        // then
        assertFalse(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertNull(result.getBindingId());
        verify(artistRepository).findByMasterId(masterId);
        verify(artistRepository).deleteByMasterId(masterId);
    }

    @Test
    void unbind_shouldReturnUnboundResult_whenArtistNotExists() {
        // given
        Long masterId = 5L;

        when(artistRepository.findByMasterId(masterId)).thenReturn(Optional.empty());

        // when
        BindingDto result = artistService.unbind(masterId);

        // then
        assertFalse(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertNull(result.getBindingId());
        verify(artistRepository).findByMasterId(masterId);
        verify(artistRepository, never()).deleteByMasterId(any());
    }

    @Test
    void getBinding_shouldReturnBoundResult_whenArtistExists() {
        // given
        Long masterId = 6L;
        Artist existingArtist = new Artist();
        existingArtist.setMasterId(masterId);
        // Use reflection to set id since setter is private
        try {
            var idField = Artist.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingArtist, 600L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(artistRepository.findByMasterId(masterId)).thenReturn(Optional.of(existingArtist));

        // when
        BindingDto result = artistService.getBinding(masterId);

        // then
        assertTrue(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertEquals(600L, result.getBindingId());
        verify(artistRepository).findByMasterId(masterId);
    }

    @Test
    void getBinding_shouldReturnUnboundResult_whenArtistNotExists() {
        // given
        Long masterId = 7L;

        when(artistRepository.findByMasterId(masterId)).thenReturn(Optional.empty());

        // when
        BindingDto result = artistService.getBinding(masterId);

        // then
        assertFalse(result.isBound());
        assertEquals(masterId, result.getMasterId());
        assertNull(result.getBindingId());
        verify(artistRepository).findByMasterId(masterId);
    }

    @Test
    void getBindings_shouldReturnMixedResults_whenSomeArtistsExist() {
        // given
        List<Long> masterIds = List.of(10L, 20L, 30L);
        
        Artist artist1 = new Artist();
        artist1.setMasterId(10L);
        Artist artist3 = new Artist();
        artist3.setMasterId(30L);
        
        // Use reflection to set ids since setter is private
        try {
            var idField = Artist.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(artist1, 1000L);
            idField.set(artist3, 3000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Artist> boundArtists = List.of(artist1, artist3);
        when(artistRepository.findByMasterIdIn(masterIds)).thenReturn(boundArtists);

        // when
        List<BindingDto> results = artistService.getBindings(masterIds);

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
        
        verify(artistRepository).findByMasterIdIn(masterIds);
    }

    @Test
    void getBindings_shouldReturnAllUnboundResults_whenNoArtistsExist() {
        // given
        List<Long> masterIds = List.of(100L, 200L);

        when(artistRepository.findByMasterIdIn(masterIds)).thenReturn(List.of());

        // when
        List<BindingDto> results = artistService.getBindings(masterIds);

        // then
        assertEquals(2, results.size());
        
        results.forEach(result -> {
            assertFalse(result.isBound());
            assertNull(result.getBindingId());
            assertTrue(masterIds.contains(result.getMasterId()));
        });
        
        verify(artistRepository).findByMasterIdIn(masterIds);
    }
}
