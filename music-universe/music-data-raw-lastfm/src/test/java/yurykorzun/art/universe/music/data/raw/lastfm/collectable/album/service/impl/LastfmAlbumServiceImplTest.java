package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumServiceImplTest {
    
    @Mock
    private LastfmAlbumRepository albumRepository;

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
    void findDtoById_shouldReturnDtoWhenAlbumExists() {
        // Given
        long albumId = 42L;
        LastfmAlbum album = createAlbum(b -> b.id(albumId).name("Test Album"));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        // When
        LastfmAlbumResponseDto result = albumService.findDtoById(albumId);

        // Then
        assertNotNull(result);
        assertEquals(albumId, result.id());
        assertEquals("Test Album", result.name());
        verify(albumRepository).findById(albumId);
    }

    @Test
    void findDtoById_shouldThrowEntityNotFoundException_whenAlbumDoesNotExist() {
        // Given
        long albumId = 999L;
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> albumService.findDtoById(albumId));
        
        assertEquals("Album not found with id: " + albumId, exception.getMessage());
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

    @Test
    void findAllByUrls_withValidUrls_shouldCallRepository() {
        final int albumsNumber = 3;
        List<String> urls = IntStream.range(0, albumsNumber).mapToObj(i -> UUID.randomUUID().toString()).toList();
        List<LastfmAlbum> albums = urls.stream()
            .map(url -> createAlbum(builder -> builder.url(url)))
            .toList();
        when(albumRepository.findAllByUrlIn(urls)).thenReturn(albums);

        List<LastfmAlbum> foundAlbums = albumService.findAllByUrls(urls);

        assertNotNull(foundAlbums);
        assertEquals(albums.size(), foundAlbums.size());
        assertEquals(albums, foundAlbums);
        verify(albumRepository, times(1)).findAllByUrlIn(urls);
    }
    
    @Test
    void findAll_shouldCallRepositoryWithCorrectParams() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 1L;
        Set<Integer> approvalStatusCodes = Set.of(ApprovalStatus.APPROVED.getCode());
        List<ApprovalStatus> approvalStatuses = CodedRegistry.getByCodes(approvalStatusCodes, ApprovalStatus.class);
        
        AlbumSearchParams params = new AlbumSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatusCodes, null);
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmArtist artist = EntityCreationHelper.createArtist(b -> b.id(artistId));
        List<LastfmAlbum> albums = List.of(
            createAlbum(b -> b.artist(artist)),
            createAlbum(b -> b.artist(artist))
        );
        Page<LastfmAlbum> albumPage = new PageImpl<>(albums, pageable, albums.size());
        
        when(albumRepository.findAlbums(
            eq(search), 
            eq(minPlayCount), 
            eq(minListenersCount),
            eq(artistId),
            eq(approvalStatuses),
            eq(params.tagId()),
            eq(pageable)
        )).thenReturn(albumPage);
        
        // When
        Page<LastfmAlbumResponseDto> result = albumService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(albums.size(), result.getContent().size());
        
        // Verify artist references in DTOs
        for (LastfmAlbumResponseDto dto : result.getContent()) {
            assertNotNull(dto.artist());
            assertEquals(artistId, dto.artist().id());
        }
        
        verify(albumRepository).findAlbums(
            eq(search), 
            eq(minPlayCount), 
            eq(minListenersCount),
            eq(artistId),
            eq(approvalStatuses),
            eq(params.tagId()),
            eq(pageable)
        );
    }
    
    @Test
    void findAll_withNullParams_shouldCallRepositoryWithNullValues() {
        // Given
        AlbumSearchParams params = new AlbumSearchParams(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        List<ApprovalStatus> expectedApprovalStatuses = Collections.emptyList();

        LastfmArtist artist = EntityCreationHelper.createArtist();
        List<LastfmAlbum> albums = List.of(
            createAlbum(b -> b.artist(artist)),
            createAlbum(b -> b.artist(artist))
        );
        Page<LastfmAlbum> albumPage = new PageImpl<>(albums, pageable, albums.size());
        
        when(albumRepository.findAlbums(
            eq(null), 
            eq(null), 
            eq(null),
            eq(null),
            eq(expectedApprovalStatuses),
            eq(params.tagId()),
            eq(pageable)
        )).thenReturn(albumPage);
        
        // When
        Page<LastfmAlbumResponseDto> result = albumService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(albums.size(), result.getContent().size());
        
        // Verify artist references in DTOs
        for (LastfmAlbumResponseDto dto : result.getContent()) {
            assertNotNull(dto.artist());
            assertEquals(artist.getId(), dto.artist().id());
        }
        
        verify(albumRepository).findAlbums(
            eq(null), 
            eq(null), 
            eq(null),
            eq(null),
            eq(expectedApprovalStatuses),
            eq(params.tagId()),
            eq(pageable)
        );
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedAlbum() {
        long albumId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist artist = EntityCreationHelper.createArtist();
        LastfmAlbum existing = createAlbum(b -> b.id(albumId).approvalStatus(oldStatus).artist(artist));
        LastfmAlbum updated = createAlbum(b -> b.id(albumId).approvalStatus(newStatus).artist(artist));

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existing));
        when(albumRepository.save(any(LastfmAlbum.class))).thenReturn(updated);

        LastfmAlbumResponseDto result = albumService.updateApprovalStatus(albumId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        assertNotNull(result.artist());
        assertEquals(artist.getId(), result.artist().id());
        verify(albumRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingAlbum_shouldThrowException() {
        when(albumRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> albumService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> albumService.updateApprovalStatus(1L, -1)
        );
    }
    
    @Test
    void findAlbumsForGetInfo_shouldCallRepository() {
        // given
        List<LastfmAlbum> expectedAlbums = List.of(createAlbum(), createAlbum(), createAlbum());
        when(albumRepository.findAlbumsForGetInfo()).thenReturn(expectedAlbums);
        
        // when
        List<LastfmAlbum> result = albumService.findAlbumsForGetInfo();
        
        // then
        verify(albumRepository, times(1)).findAlbumsForGetInfo();
        assertEquals(expectedAlbums, result, "Should return albums from repository");
    }
}
