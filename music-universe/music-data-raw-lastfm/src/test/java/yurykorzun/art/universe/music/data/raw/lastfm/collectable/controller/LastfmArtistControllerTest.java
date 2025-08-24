package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller.LastfmArtistController;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup.LastfmArtistLookupService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmArtistControllerTest {

    @Mock
    private LastfmArtistService artistService;

    @Mock
    private LastfmArtistLookupService artistLookupService;

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
        Long tagId = 123L;
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

        ArtistSearchParams expectedParams = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses, tagId);
        when(artistService.findAll(eq(expectedParams), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmArtistResponseDto> result = controller.getArtists(search, minPlayCount, minListenersCount, approvalStatuses, tagId, pageable);

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
        Page<LastfmArtistResponseDto> result = controller.getArtists(null, null, null, null, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getArtistById_shouldReturnArtistWhenFound() {
        // given
        Long artistId = 1L;
        LastfmArtistResponseDto responseDto = new LastfmArtistResponseDto(
            artistId, "Test Artist", "https://example.com/artist", "mbid123", 
            ApprovalStatus.APPROVED.getCode(), 5000L, 1000);

        when(artistService.findDtoById(artistId)).thenReturn(responseDto);

        // when
        LastfmArtistResponseDto result = controller.getArtistById(artistId);

        // then
        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedArtist() {
        // given
        Long artistId = 1L;
        ApprovalStatus newApprovalStatus = ApprovalStatus.APPROVED;
        int approvalStatusCode = newApprovalStatus.getCode();

        LastfmArtistResponseDto responseDto = new LastfmArtistResponseDto(
            artistId, "Test Artist", "https://example.com/artist", "mbid123", 
            approvalStatusCode, 5000L, 1000);

        when(artistService.updateApprovalStatus(artistId, approvalStatusCode))
            .thenReturn(responseDto);

        // when
        LastfmArtistResponseDto result = controller.updateApprovalStatus(artistId, new ApprovalStatusRequestDto(approvalStatusCode));

        // then
        assertNotNull(result);
        assertEquals(newApprovalStatus.getCode(), result.approvalStatus());
    }

    @Test
    void lookupArtists_shouldReturnLookupResults() {
        // given
        String search = "Beatles";
        Integer limit = 10;
        
        List<LookupResultDTO> expectedResults = List.of(
            LookupResultDTO.builder().id(1L).name("The Beatles").build(),
            LookupResultDTO.builder().id(2L).name("Beatles Revival").build()
        );

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();

        when(artistLookupService.lookup(eq(expectedRequest)))
            .thenReturn(expectedResults);

        // when
        List<LookupResultDTO> result = controller.lookupArtists(search, limit);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("The Beatles", result.get(0).getName());
        assertEquals("Beatles Revival", result.get(1).getName());
    }

    @Test
    void lookupArtists_shouldHandleNullLimit() {
        // given
        String search = "Beatles";
        
        List<LookupResultDTO> expectedResults = List.of(
            LookupResultDTO.builder().id(1L).name("The Beatles").build()
        );

        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(null)
            .build();

        when(artistLookupService.lookup(eq(expectedRequest)))
            .thenReturn(expectedResults);

        // when
        List<LookupResultDTO> result = controller.lookupArtists(search, null);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("The Beatles", result.get(0).getName());
    }

    @Test
    void lookupArtists_shouldReturnEmptyListWhenNoMatches() {
        // given
        String search = "NonExistentArtist";
        Integer limit = 10;
        
        LookupRequestDTO expectedRequest = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();

        when(artistLookupService.lookup(eq(expectedRequest)))
            .thenReturn(List.of());

        // when
        List<LookupResultDTO> result = controller.lookupArtists(search, limit);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
