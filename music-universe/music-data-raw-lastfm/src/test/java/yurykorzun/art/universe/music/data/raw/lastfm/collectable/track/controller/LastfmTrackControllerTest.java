package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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
        LastfmTrack track = LastfmTrack.builder()
            .id(trackId)
            .name("Test Track")
            .url("https://example.com/track")
            .mbid("mbid123")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(5000L)
            .listenersCount(1000)
            .artist(artist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        when(trackService.findById(trackId)).thenReturn(Optional.of(track));

        // when
        ResponseEntity<ResponseWrapper<LastfmTrackResponseDto>> response = controller.getTrackById(trackId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<LastfmTrackResponseDto> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        LastfmTrackResponseDto data = body.getData();
        assertNotNull(data);
        compareDtoAgainstEntity(data, track);
    }

    @Test
    void getTrackById_shouldReturnNotFoundWhenTrackDoesNotExist() {
        // given
        Long trackId = 999L;
        when(trackService.findById(trackId)).thenReturn(Optional.empty());

        // when
        ResponseEntity<ResponseWrapper<LastfmTrackResponseDto>> response = controller.getTrackById(trackId);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseWrapper<LastfmTrackResponseDto> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Track not found"));
    }

    @Test
    void getTrackById_shouldReturnErrorWhenExceptionOccurs() {
        // given
        Long trackId = 1L;
        when(trackService.findById(trackId)).thenThrow(new RuntimeException("Database error"));

        // when
        ResponseEntity<ResponseWrapper<LastfmTrackResponseDto>> response = controller.getTrackById(trackId);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<LastfmTrackResponseDto> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to fetch track"));
    }

    @Test
    void getTracks_shouldReturnDtoPageWrappedInResponse() {
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
        ResponseEntity<ResponseWrapper<Page<LastfmTrackResponseDto>>> response = 
            controller.getTracks(search, minPlayCount, minListenersCount, artistId, approvalStatuses, pageable);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<Page<LastfmTrackResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        Page<LastfmTrackResponseDto> data = body.getData();
        assertNotNull(data);
        assertEquals(2, data.getTotalElements());

        for (int i = 0; i < trackList.size(); i++) {
            LastfmTrackResponseDto dto = data.getContent().get(i);
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

        verify(trackService).findAll(expectedParams, pageable);
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
        ResponseEntity<ResponseWrapper<Page<LastfmTrackResponseDto>>> response = 
            controller.getTracks(null, null, null, null, null, pageable);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<Page<LastfmTrackResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        
        Page<LastfmTrackResponseDto> data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.getTotalElements());
    }

    @Test
    void getTracks_shouldReturnFailureOnException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(trackService.findAll(any(TrackSearchParams.class), any())).thenThrow(new RuntimeException("Fail"));

        // when
        ResponseEntity<ResponseWrapper<Page<LastfmTrackResponseDto>>> response = 
            controller.getTracks("abc", null, null, null, null, pageable);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<?> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to fetch tracks"));
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTrack() {
        Long trackId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmArtist artist = EntityCreationHelper.createArtist();
        LastfmTrack track = EntityCreationHelper.createTrack(b -> 
            b.id(trackId).approvalStatus(newApprovalStatus).artist(artist));
            
        LastfmTrackResponseDto dto = LastfmTrackResponseDto.from(track);

        when(trackService.updateApprovalStatus(trackId, approvalStatusCode))
            .thenReturn(dto);

        ResponseEntity<ResponseWrapper<LastfmTrackResponseDto>> response =
            controller.updateApprovalStatus(trackId, new ApprovalStatusRequestDto(approvalStatusCode));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<LastfmTrackResponseDto> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        LastfmTrackResponseDto data = body.getData();
        assertNotNull(data);
        assertEquals(newApprovalStatus.getCode(), data.approvalStatus());
        assertNotNull(data.artist());
        assertEquals(artist.getId(), data.artist().id());
    }

    @Test
    void updateApprovalStatus_shouldHandleServiceException() {
        Long trackId = 1L;
        when(trackService.updateApprovalStatus(anyLong(), anyInt()))
            .thenThrow(new IllegalArgumentException("Invalid status"));

        ResponseEntity<ResponseWrapper<LastfmTrackResponseDto>> response =
            controller.updateApprovalStatus(trackId, new ApprovalStatusRequestDto(999));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<LastfmTrackResponseDto> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to update approval status"));
    }
}
