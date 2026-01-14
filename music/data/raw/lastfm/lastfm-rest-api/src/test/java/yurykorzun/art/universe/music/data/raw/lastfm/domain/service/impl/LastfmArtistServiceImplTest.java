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
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistServiceImplTest {
    
    @Mock
    private LastfmArtistRepository artistRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
    void findById_shouldReturnDtoByIdWhenArtistExists() {
        // Given
        long artistId = 42L;
        LastfmArtist artist = createArtist(b -> b.id(artistId).name("Test Artist"));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        // When
        LastfmArtistResponseDto result = artistService.findById(artistId);

        // Then
        assertNotNull(result);
        assertEquals(artistId, result.id());
        assertEquals("Test Artist", result.name());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenArtistDoesNotExist() {
        // Given
        long artistId = 999L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> artistService.findById(artistId));
        
        assertEquals("Artist not found with id: " + artistId, exception.getMessage());
        verify(artistRepository).findById(artistId);
    }
    
    @Test
    void findAll_shouldCallRepositoryWithCorrectParams() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Set<Integer> approvalStatusCodes = Set.of(ApprovalStatus.APPROVED.getCode());
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(approvalStatusCodes);
        
        ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatusCodes, null);
        Pageable pageable = PageRequest.of(0, 10);
        
        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        Page<LastfmArtist> artistPage = new PageImpl<>(artists, pageable, artists.size());
        
        when(artistRepository.findArtists(
            eq(search), 
            eq(minPlayCount), 
            eq(minListenersCount), 
            eq(approvalStatuses),
            eq(params.tagId()),
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
            eq(params.tagId()),
            eq(pageable)
        );
    }
    
    @Test
    void findAll_withNullParams_shouldCallRepositoryWithNullValues() {
        // Given
        ArtistSearchParams params = new ArtistSearchParams(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        List<ApprovalStatus> expectedApprovalStatuses = Collections.emptyList();

        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        Page<LastfmArtist> artistPage = new PageImpl<>(artists, pageable, artists.size());
        
        when(artistRepository.findArtists(
            eq(null), 
            eq(null), 
            eq(null), 
            eq(expectedApprovalStatuses),
            eq(params.tagId()),
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
            eq(params.tagId()),
            eq(pageable)
        );
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(Collection<Integer> codes) {
        return CodedRegistry.getByCodes(codes, ApprovalStatus.class);
    }
}
