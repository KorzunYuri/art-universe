package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumServiceImplTest {
    
    @Mock
    private LastfmAlbumRepository albumRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LastfmAlbumServiceImpl albumService;

    private LastfmAlbum createAlbum() {
        return EntityCreationHelper.createAlbum();
    }

    private LastfmAlbum createAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?,?>> customizer) {
        return EntityCreationHelper.createAlbum(customizer);
    }

    @Test
    void findById_shouldReturnAlbumWhenExists() {
        // Given
        long albumId = 42L;
        LastfmAlbum expectedAlbum = createAlbum(b -> b.id(albumId));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(expectedAlbum));

        // When
        Optional<LastfmAlbum> result = albumService.findById(albumId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedAlbum, result.get());
        verify(albumRepository).findById(albumId);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenAlbumDoesNotExist() {
        // Given
        long albumId = 999L;
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When
        Optional<LastfmAlbum> result = albumService.findById(albumId);

        // Then
        assertFalse(result.isPresent());
        verify(albumRepository).findById(albumId);
    }

    @Test
    void saveAll_withValidAlbums_shouldCallRepository() {
        List<LastfmAlbum> albums = List.of(createAlbum(), createAlbum());
        when(albumRepository.saveAll(albums)).thenReturn(albums);

        List<LastfmAlbum> savedAlbums = albumService.saveAll(albums);

        assertNotNull(savedAlbums);
        assertEquals(albums.size(), savedAlbums.size());
        assertEquals(albums, savedAlbums);
        verify(albumRepository, times(1)).saveAll(albums);
    }
}
