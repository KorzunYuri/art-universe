package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistServiceImplTest {
    
    @Mock
    private LastfmArtistRepository artistRepository;

    @InjectMocks
    private LastfmArtistServiceImpl artistService;

    private LastfmArtist createArtist() {
        return EntityCreationHelper.createArtist();
    }
    private LastfmArtist createArtist(String url) {
        return EntityCreationHelper.createArtist(builder -> builder.url(url));
    }


    private LastfmArtist createArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?,?>> overrideDefaults) {
        LastfmArtist.LastfmArtistBuilder<?, ?> builder = LastfmArtist.builder()
            .id(1L)
            .name("Test Artist")
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(mock(LastfmApiCall.class));
        overrideDefaults.accept(builder);
        return builder.build();
    }

    @Test
    void findById_shouldReturnArtistWhenExists() {
        // Given
        long artistId = 42L;
        LastfmArtist expectedArtist = createArtist(b -> b.id(artistId));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(expectedArtist));

        // When
        Optional<LastfmArtist> result = artistService.findById(artistId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedArtist, result.get());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void findDto_shouldReturnDtoByIdWhenArtistExists() {
        // Given
        long artistId = 42L;
        LastfmArtist artist = createArtist(b -> b.id(artistId).name("Test Artist"));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        // When
        LastfmArtistResponseDto result = artistService.findDtoById(artistId);

        // Then
        assertNotNull(result);
        assertEquals(artistId, result.id());
        assertEquals("Test Artist", result.name());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void findDto_ById_shouldThrowEntityNotFoundException_whenArtistDoesNotExist() {
        // Given
        long artistId = 999L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> artistService.findDtoById(artistId));
        
        assertEquals("Artist not found with id: " + artistId, exception.getMessage());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenArtistDoesNotExist() {
        // Given
        long artistId = 999L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When
        Optional<LastfmArtist> result = artistService.findById(artistId);

        // Then
        assertFalse(result.isPresent());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void save_withValid_shouldCallRepository() {
        LastfmArtist artist = createArtist();
        when(artistRepository.save(artist)).thenReturn(artist);

        LastfmArtist savedArtist = artistService.save(artist);

        assertNotNull(savedArtist);
        assertEquals(artist, savedArtist);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void saveAll_withValidAll_shouldCallRepository() {
        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        when(artistRepository.saveAll(artists)).thenReturn(artists);

        List<LastfmArtist> savedArtists = artistService.saveAll(artists);

        assertNotNull(savedArtists);
        assertEquals(artists.size(), savedArtists.size());
        assertEquals(artists, savedArtists);
        verify(artistRepository, times(1)).saveAll(artists);
    }

    @Test
    void findAllByUrls_withValidUrls_shouldCallRepository() {
        final int artistsNumber = 3;
        List<String> names = IntStream.range(0, artistsNumber).mapToObj(i -> UUID.randomUUID().toString()).toList();
        List<LastfmArtist> artists = names.stream()
            .map(this::createArtist)
            .toList();
        when(artistRepository.findAllByNameIn(names)).thenReturn(artists);

        List<LastfmArtist> foundArtists = artistService.findAllByNames(names);

        assertNotNull(foundArtists);
        assertEquals(artists.size(), foundArtists.size());
        assertEquals(artists, foundArtists);
        verify(artistRepository, times(1)).findAllByNameIn(names);
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedArtist() {
        long artistId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist existing = createArtist(b -> b.approvalStatus(oldStatus));
        LastfmArtist updated = createArtist(b -> b.approvalStatus(newStatus));

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(existing));
        when(artistRepository.save(any(LastfmArtist.class))).thenReturn(updated);

        LastfmArtistResponseDto result = artistService.updateApprovalStatus(artistId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        verify(artistRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingArtist_shouldThrowException() {
        when(artistRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> artistService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> artistService.updateApprovalStatus(1L, -1)
        );
    }
    
    @Test
    void findAll_shouldCallRepositoryWithCorrectParams() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Set<Integer> approvalStatusCodes = Set.of(ApprovalStatus.APPROVED.getCode());
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(approvalStatusCodes);
        
        ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatusCodes);
        Pageable pageable = PageRequest.of(0, 10);
        
        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        Page<LastfmArtist> artistPage = new PageImpl<>(artists, pageable, artists.size());
        
        when(artistRepository.findArtists(
            eq(search), 
            eq(minPlayCount), 
            eq(minListenersCount), 
            eq(approvalStatuses),
            eq(pageable)
        )).thenReturn(artistPage);
        
        // When
        Page<LastfmArtistResponseDto> result = artistService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(artists.size(), result.getContent().size());
        verify(artistRepository).findArtists(
            eq(search), 
            eq(minPlayCount), 
            eq(minListenersCount), 
            eq(approvalStatuses),
            eq(pageable)
        );
    }
    
    @Test
    void findAll_withNullParams_shouldCallRepositoryWithNullValues() {
        // Given
        ArtistSearchParams params = new ArtistSearchParams(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        List<ApprovalStatus> expectedApprovalStatuses = Collections.emptyList();

        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        Page<LastfmArtist> artistPage = new PageImpl<>(artists, pageable, artists.size());
        
        when(artistRepository.findArtists(
            eq(null), 
            eq(null), 
            eq(null), 
            eq(expectedApprovalStatuses),
            eq(pageable)
        )).thenReturn(artistPage);
        
        // When
        Page<LastfmArtistResponseDto> result = artistService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(artists.size(), result.getContent().size());
        verify(artistRepository).findArtists(
            eq(null), 
            eq(null), 
            eq(null), 
            eq(expectedApprovalStatuses),
            eq(pageable)
        );
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(Collection<Integer> codes) {
        return CodedRegistry.getByCodes(codes, ApprovalStatus.class);
    }
}
