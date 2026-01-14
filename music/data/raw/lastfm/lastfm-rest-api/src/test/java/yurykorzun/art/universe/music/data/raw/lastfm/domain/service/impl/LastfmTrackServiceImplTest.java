package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

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
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    
    private LastfmTrack createTrack(String url) {
        return EntityCreationHelper.createTrack(url);
    }

    private LastfmTrack createTrack(Consumer<LastfmTrack.LastfmTrackBuilder<?,?>> customizer) {
        return EntityCreationHelper.createTrack(customizer);
    }

    @Test
    void findById_shouldReturnDtoByIdWhenTrackExists() {
        // Given
        long trackId = 42L;
        LastfmTrack track = createTrack(b -> b.id(trackId).name("Test Track"));
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

        // When
        LastfmTrackResponseDto result = trackService.findById(trackId);

        // Then
        assertNotNull(result);
        assertEquals(trackId, result.id());
        assertEquals("Test Track", result.name());
        verify(trackRepository).findById(trackId);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenTrackDoesNotExist() {
        // Given
        long trackId = 999L;
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> trackService.findById(trackId));
        
        assertEquals("Track not found with id: " + trackId, exception.getMessage());
        verify(trackRepository).findById(trackId);
    }

    @Test
    void findAll_WithCorrectParams_shouldCallRepository() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 1L;
        Set<Integer> approvalStatusCodes = Set.of(ApprovalStatus.APPROVED.getCode());
        List<ApprovalStatus> approvalStatuses = CodedRegistry.getByCodes(approvalStatusCodes, ApprovalStatus.class);
        
        TrackSearchParams params = new TrackSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatusCodes, null);
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmArtist artist = EntityCreationHelper.createArtist(b -> b.id(artistId));
        List<LastfmTrack> tracks = List.of(
            createTrack(b -> b.artist(artist)),
            createTrack(b -> b.artist(artist))
        );
        Page<LastfmTrack> trackPage = new PageImpl<>(tracks, pageable, tracks.size());
        
        when(trackRepository.findTracks(
            eq(search), 
            eq(minPlayCount), 
            eq(minListenersCount),
            eq(artistId),
            eq(approvalStatuses),
            eq(params.tagId()),
            eq(pageable)
        )).thenReturn(trackPage);
        
        // When
        Page<LastfmTrackResponseDto> result = trackService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(tracks.size(), result.getContent().size());
        
        // Verify artist references in DTOs
        for (LastfmTrackResponseDto dto : result.getContent()) {
            assertNotNull(dto.artist());
            assertEquals(artistId, dto.artist().id());
        }
        
        verify(trackRepository).findTracks(
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
        TrackSearchParams params = new TrackSearchParams(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        List<ApprovalStatus> expectedApprovalStatuses = Collections.emptyList();

        LastfmArtist artist = EntityCreationHelper.createArtist();
        List<LastfmTrack> tracks = List.of(
            createTrack(b -> b.artist(artist)),
            createTrack(b -> b.artist(artist))
        );
        Page<LastfmTrack> trackPage = new PageImpl<>(tracks, pageable, tracks.size());
        
        when(trackRepository.findTracks(
            eq(null), 
            eq(null), 
            eq(null),
            eq(null),
            eq(expectedApprovalStatuses),
            eq(params.tagId()),
            eq(pageable)
        )).thenReturn(trackPage);
        
        // When
        Page<LastfmTrackResponseDto> result = trackService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(tracks.size(), result.getContent().size());
        
        // Verify artist references in DTOs
        for (LastfmTrackResponseDto dto : result.getContent()) {
            assertNotNull(dto.artist());
            assertEquals(artist.getId(), dto.artist().id());
        }
        
        verify(trackRepository).findTracks(
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
