package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

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
    void findDtoById_shouldReturnDtoWhenAlbumExists() {
        // Given
        long albumId = 42L;
        LastfmAlbum album = createAlbum(b -> b.id(albumId).name("Test Album"));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        // When
        LastfmAlbumResponseDto result = albumService.findById(albumId);

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
            () -> albumService.findById(albumId));

        assertEquals("Album not found with id: " + albumId, exception.getMessage());
        verify(albumRepository).findById(albumId);
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
}
