package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.common.exception.DataFetchException;
import yurykorzun.art.universe.common.exception.DataUpdateException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmArtistControllerTest {

    @Mock
    private LastfmArtistService artistService;

    @InjectMocks
    private LastfmArtistController controller;

    private void compareDtoAgainstEntity(LastfmArtistResponseDto dto, LastfmArtist entity) {
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getUrl(), dto.url());
        assertEquals(entity.getMbid(), dto.mbid());
        assertEquals(entity.getPlayCount(), dto.playCount());
        assertEquals(entity.getListenersCount(), dto.listenersCount());
    }

    @Test
    void getArtists_shouldReturnDtoPage() {
        // given
        String search = "radiohead";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        LastfmArtist artist1 = LastfmArtist.builder()
            .id(1L)
            .name("Radiohead")
            .url("https://example.com/radiohead")
            .mbid("mbid1")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(1000L)
            .listenersCount(500)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        LastfmArtist artist2 = LastfmArtist.builder()
            .id(2L)
            .name("Radioriot")
            .url("https://example.com/radioriot")
            .mbid(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(null)
            .listenersCount(null)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        List<LastfmArtist> artistList = List.of(artist1, artist2);
        Page<LastfmArtist> artistPage = new PageImpl<>(artistList, pageable, artistList.size());
        Page<LastfmArtistResponseDto> dtoPage = artistPage.map(LastfmArtistResponseDto::from);

        ArtistSearchParams expectedParams = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses);
        when(artistService.findAll(eq(expectedParams), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmArtistResponseDto> result = controller.getArtists(search, minPlayCount, minListenersCount, approvalStatuses, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        for (int i = 0; i < artistList.size(); i++) {
            compareDtoAgainstEntity(result.getContent().get(i), artistList.get(i));
        }
    }

    @Test
    void getArtists_shouldHandleNullFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmArtist artist = EntityCreationHelper.createArtist();
        Page<LastfmArtist> artistPage = new PageImpl<>(List.of(artist), pageable, 1);
        Page<LastfmArtistResponseDto> dtoPage = artistPage.map(LastfmArtistResponseDto::from);
        
        when(artistService.findAll(any(ArtistSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmArtistResponseDto> result = controller.getArtists(null, null, null, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getArtists_shouldThrowDataFetchExceptionOnException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(artistService.findAll(any(ArtistSearchParams.class), any())).thenThrow(new RuntimeException("Fail"));

        // when & then
        DataFetchException exception = assertThrows(DataFetchException.class, () -> {
            controller.getArtists("abc", null, null, null, pageable);
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch artists"));
    }

    @Test
    void getArtistById_shouldReturnArtistWhenFound() {
        // given
        Long artistId = 1L;
        LastfmArtist artist = LastfmArtist.builder()
            .id(artistId)
            .name("Test Artist")
            .url("https://example.com/artist")
            .mbid("mbid123")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(5000L)
            .listenersCount(1000)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        when(artistService.findById(artistId)).thenReturn(Optional.of(artist));

        // when
        LastfmArtistResponseDto result = controller.getArtistById(artistId);

        // then
        assertNotNull(result);
        compareDtoAgainstEntity(result, artist);
    }

    @Test
    void getArtistById_shouldThrowEntityNotFoundExceptionWhenArtistDoesNotExist() {
        // given
        Long artistId = 999L;
        when(artistService.findById(artistId)).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            controller.getArtistById(artistId);
        });
        
        assertTrue(exception.getMessage().contains("Artist not found"));
    }

    @Test
    void getArtistById_shouldThrowDataFetchExceptionWhenExceptionOccurs() {
        // given
        Long artistId = 1L;
        when(artistService.findById(artistId)).thenThrow(new RuntimeException("Database error"));

        // when & then
        DataFetchException exception = assertThrows(DataFetchException.class, () -> {
            controller.getArtistById(artistId);
        });
        
        assertTrue(exception.getMessage().contains("Failed to fetch artist"));
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedArtist() {
        Long artistId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmArtist artist = EntityCreationHelper.createArtist(b -> b.approvalStatus(newApprovalStatus));
        LastfmArtistResponseDto responseDto = LastfmArtistResponseDto.from(artist);

        when(artistService.updateApprovalStatus(artistId, approvalStatusCode))
            .thenReturn(responseDto);

        LastfmArtistResponseDto result = controller.updateApprovalStatus(artistId, new ApprovalStatusRequestDto(approvalStatusCode));

        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }

    @Test
    void updateApprovalStatus_shouldThrowDataUpdateExceptionOnServiceException() {
        Long artistId = 1L;
        when(artistService.updateApprovalStatus(anyLong(), anyInt()))
            .thenThrow(new IllegalArgumentException("Invalid status"));

        DataUpdateException exception = assertThrows(DataUpdateException.class, () -> {
            controller.updateApprovalStatus(artistId, new ApprovalStatusRequestDto(999));
        });
        
        assertTrue(exception.getMessage().contains("Failed to update approval status"));
    }
}
