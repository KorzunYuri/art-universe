package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

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
    
    private LastfmTrack createTrack(String url) {
        return EntityCreationHelper.createTrack(url);
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
    void findDto_shouldReturnDtoByIdWhenTrackExists() {
        // Given
        long trackId = 42L;
        LastfmTrack track = createTrack(b -> b.id(trackId).name("Test Track"));
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

        // When
        LastfmTrackResponseDto result = trackService.findDtoById(trackId);

        // Then
        assertNotNull(result);
        assertEquals(trackId, result.id());
        assertEquals("Test Track", result.name());
        verify(trackRepository).findById(trackId);
    }

    @Test
    void findDto_ById_shouldThrowEntityNotFoundException_whenTrackDoesNotExist() {
        // Given
        long trackId = 999L;
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> trackService.findDtoById(trackId));
        
        assertEquals("Track not found with id: " + trackId, exception.getMessage());
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
    void save_withValidTrack_shouldCallRepository() {
        LastfmTrack track = createTrack();
        when(trackRepository.save(track)).thenReturn(track);

        LastfmTrack savedTrack = trackService.save(track);

        assertNotNull(savedTrack);
        assertEquals(track, savedTrack);
        verify(trackRepository, times(1)).save(track);
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

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTrack() {
        long trackId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist artist = EntityCreationHelper.createArtist();
        LastfmTrack existing = createTrack(b -> b.id(trackId).approvalStatus(oldStatus).artist(artist));
        LastfmTrack updated = createTrack(b -> b.id(trackId).approvalStatus(newStatus).artist(artist));

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(existing));
        when(trackRepository.save(any(LastfmTrack.class))).thenReturn(updated);

        LastfmTrackResponseDto result = trackService.updateApprovalStatus(trackId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        assertNotNull(result.artist());
        assertEquals(artist.getId(), result.artist().id());
        verify(trackRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingTrack_shouldThrowException() {
        when(trackRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> trackService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> trackService.updateApprovalStatus(1L, -1)
        );
    }
}
