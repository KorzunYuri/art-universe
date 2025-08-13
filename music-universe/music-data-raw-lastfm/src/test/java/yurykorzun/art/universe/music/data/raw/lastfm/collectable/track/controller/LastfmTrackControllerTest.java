package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmTrackControllerTest {

    @Mock
    private LastfmTrackService trackService;

    @InjectMocks
    private LastfmTrackController controller;

    private void compareDtoAgainstEntity(LastfmTrackResponseDto dto, LastfmTrack entity) {
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getUrl(), dto.url());
        assertEquals(entity.getMbid(), dto.mbid());
        assertEquals(entity.getPlayCount(), dto.playCount());
        assertEquals(entity.getListenersCount(), dto.listenersCount());
        
        // Check artist reference
        if (entity.getArtist() != null) {
            assertNotNull(dto.artist());
            assertEquals(entity.getArtist().getId(), dto.artist().id());
            assertEquals(entity.getArtist().getName(), dto.artist().name());
        } else {
            assertNull(dto.artist());
        }
    }

    @Test
    void getTrackById_shouldReturnTrackWhenFound() {
        // given
        Long trackId = 1L;
        LastfmArtist artist = EntityCreationHelper.createArtist(b -> b.id(2L).name("Test Artist"));
        LastfmTrackResponseDto responseDto = new LastfmTrackResponseDto(
            trackId, "Test Track", "https://example.com/track", "mbid123", 
            ApprovalStatus.APPROVED.getCode(), 1000, 5000L, 
            new yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto(
                2L, "Test Artist", "https://example.com/artist", "artist-mbid", 
                ApprovalStatus.APPROVED.getCode(), 10000L, 5000));

        when(trackService.findDtoById(trackId)).thenReturn(responseDto);

        // when
        LastfmTrackResponseDto result = controller.getTrackById(trackId);

        // then
        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void getTracks_shouldReturnDtoPage() {
        // given
        String search = "test track";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 1L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.id(artistId));
        
        LastfmTrack track1 = LastfmTrack.builder()
            .id(1L)
            .name("Test Track 1")
            .url("https://example.com/track1")
            .mbid("mbid1")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(1000L)
            .listenersCount(500)
            .artist(artist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        LastfmTrack track2 = LastfmTrack.builder()
            .id(2L)
            .name("Test Track 2")
            .url("https://example.com/track2")
            .mbid(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(null)
            .listenersCount(null)
            .artist(artist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        List<LastfmTrack> trackList = List.of(track1, track2);
        Page<LastfmTrack> trackPage = new PageImpl<>(trackList, pageable, trackList.size());
        Page<LastfmTrackResponseDto> dtoPage = trackPage.map(LastfmTrackResponseDto::from);

        TrackSearchParams expectedParams = new TrackSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatuses);
        when(trackService.findAll(any(TrackSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmTrackResponseDto> result = controller.getTracks(search, minPlayCount, minListenersCount, artistId, approvalStatuses, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        for (int i = 0; i < trackList.size(); i++) {
            LastfmTrackResponseDto dto = result.getContent().get(i);
            LastfmTrack entity = trackList.get(i);
            
            assertEquals(entity.getId(), dto.id());
            assertEquals(entity.getName(), dto.name());
            assertEquals(entity.getUrl(), dto.url());
            assertEquals(entity.getMbid(), dto.mbid());
            assertEquals(entity.getApprovalStatus().getCode(), dto.approvalStatus());
            
            // Check artist reference
            assertNotNull(dto.artist());
            assertEquals(entity.getArtist().getId(), dto.artist().id());
            assertEquals(entity.getArtist().getName(), dto.artist().name());
        }
    }
    
    @Test
    void getTracks_shouldHandleNullFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmTrack track = EntityCreationHelper.createTrack();
        Page<LastfmTrack> trackPage = new PageImpl<>(List.of(track), pageable, 1);
        Page<LastfmTrackResponseDto> dtoPage = trackPage.map(LastfmTrackResponseDto::from);
        
        when(trackService.findAll(any(TrackSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmTrackResponseDto> result = controller.getTracks(null, null, null, null, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTrack() {
        Long trackId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmTrackResponseDto responseDto = new LastfmTrackResponseDto(
            trackId, "Test Track", "https://example.com/track", "mbid123", 
            approvalStatusCode, 1000, 5000L, null);

        when(trackService.updateApprovalStatus(trackId, approvalStatusCode))
            .thenReturn(responseDto);

        LastfmTrackResponseDto result = controller.updateApprovalStatus(trackId, new ApprovalStatusRequestDto(approvalStatusCode));

        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }
}
