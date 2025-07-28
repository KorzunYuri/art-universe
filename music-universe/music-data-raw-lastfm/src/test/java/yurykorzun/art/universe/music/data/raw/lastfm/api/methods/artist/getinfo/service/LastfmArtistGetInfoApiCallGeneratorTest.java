package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmArtistGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmArtistService artistService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmEntityService entityService;

    @InjectMocks
    private LastfmArtistGetInfoApiCallGenerator generator;

    @Test
    void selectEntitiesForApiCalls_shouldDeduplicateByMbid() {
        // Given: Mock service returns artists with duplicate MBIDs
        List<LastfmArtist> artistsFromService = List.of(
            createArtist(1L, "Artist1", "same-mbid", ApprovalStatus.PENDING, 1000),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.APPROVED, 500),
            createArtist(3L, "Artist3", "different-mbid", ApprovalStatus.APPROVED, 2000)
        );
        
        when(artistService.findAllToGetInfoFor()).thenReturn(artistsFromService);

        // When
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

        // Then
        assertEquals(2, result.size(), "Should deduplicate artists with same MBID");
        
        // Should keep the approved artist with same-mbid (artist2)
        assertEquals(1, result.stream()
            .mapToLong(a -> "same-mbid".equals(a.getMbid()) ? 1 : 0)
            .sum(), "Should have only one artist with same-mbid");
        
        LastfmArtist selectedSameMbid = result.stream()
            .filter(a -> "same-mbid".equals(a.getMbid()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedSameMbid.getId(), "Should select approved artist over pending");
        assertEquals(ApprovalStatus.APPROVED, selectedSameMbid.getApprovalStatus());
        
        // Should keep the different-mbid artist
        assertEquals(1, result.stream()
            .mapToLong(a -> "different-mbid".equals(a.getMbid()) ? 1 : 0)
            .sum(), "Should keep artist with different MBID");
    }

    @Test
    void selectEntitiesForApiCalls_shouldHandleEmptyList() {
        // Given
        when(artistService.findAllToGetInfoFor()).thenReturn(List.of());

        // When
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

        // Then
        assertEquals(0, result.size());
    }

    @Test
    void selectEntitiesForApiCalls_shouldPreserveOrderAfterDeduplication() {
        // Given: Artists in specific order
        List<LastfmArtist> artistsFromService = List.of(
            createArtist(1L, "First", "mbid1", ApprovalStatus.APPROVED, 3000),
            createArtist(2L, "Second", "mbid2", ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Third", "mbid1", ApprovalStatus.APPROVED, 1000) // Duplicate MBID, lower metrics
        );
        
        when(artistService.findAllToGetInfoFor()).thenReturn(artistsFromService);

        // When
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId(), "Should keep first artist with mbid1 (better metrics)");
        assertEquals(2L, result.get(1).getId(), "Should keep second artist with mbid2");
    }

    private LastfmArtist createArtist(Long id, String name, String mbid, ApprovalStatus status, Integer listenersCount) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ARTIST_GET_INFO)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(java.util.Map.of())
            .build();
            
        return LastfmArtist.builder()
            .id(id)
            .name(name)
            .mbid(mbid)
            .approvalStatus(status)
            .listenersCount(listenersCount)
            .apiCall(mockApiCall)
            .build();
    }
}
