package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmApiCallEntityService entityService;

    private TestableLastfmAlbumApiCallGenerator generator;

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByMbid() {
        // Given: Albums with duplicate MBIDs
        List<LastfmAlbum> albums = List.of(
            createAlbum(1L, "Album1", "duplicate-mbid", ApprovalStatus.PENDING, 1000),
            createAlbum(2L, "Album2", "duplicate-mbid", ApprovalStatus.APPROVED, 2000),
            createAlbum(3L, "Album3", "unique-mbid", ApprovalStatus.APPROVED, 1500)
        );
        
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmAlbum> result = generator.deduplicateEntitiesForApiCalls(albums);

        // Then
        assertEquals(2, result.size(), "Should deduplicate albums with same MBID");
        
        // Should keep the approved album with higher listeners count (album2)
        assertEquals(1, result.stream()
            .mapToLong(a -> "duplicate-mbid".equals(a.getMbid()) ? 1 : 0)
            .sum(), "Should have only one album with duplicate-mbid");
        
        LastfmAlbum selectedDuplicate = result.stream()
            .filter(a -> "duplicate-mbid".equals(a.getMbid()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved album with higher listeners count");
        assertEquals(ApprovalStatus.APPROVED, selectedDuplicate.getApprovalStatus());
        assertEquals(2000, selectedDuplicate.getListenersCount());
    }

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByNameAndArtist() {
        // Given: Albums with same name and artist but no MBID
        LastfmArtist artist = createArtist(10L, "Artist1");
        
        LastfmAlbum album1 = createAlbum(1L, "Same Album", null, ApprovalStatus.PENDING, 1000);
        album1.setArtist(artist);
        
        LastfmAlbum album2 = createAlbum(2L, "Same Album", null, ApprovalStatus.APPROVED, 2000);
        album2.setArtist(artist);
        
        LastfmAlbum album3 = createAlbum(3L, "Different Album", null, ApprovalStatus.PENDING, 1500);
        album3.setArtist(artist);
        
        List<LastfmAlbum> albums = List.of(album1, album2, album3);
        
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmAlbum> result = generator.deduplicateEntitiesForApiCalls(albums);

        // Then
        assertEquals(2, result.size(), "Should deduplicate albums with same name and artist");
        
        // Should keep the approved album with higher listeners count (album2)
        LastfmAlbum selectedDuplicate = result.stream()
            .filter(a -> "Same Album".equals(a.getName()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved album with higher listeners count");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum approvedAlbum = createAlbum(1L, "Album1", "mbid1", ApprovalStatus.APPROVED, 1000);
        LastfmAlbum pendingAlbum = createAlbum(2L, "Album2", "mbid2", ApprovalStatus.PENDING, 2000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(approvedAlbum, pendingAlbum), 
            "Approved album should have higher priority than pending album");
        assertFalse(generator.hasHigherPriority(pendingAlbum, approvedAlbum), 
            "Pending album should not have higher priority than approved album");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByListenersCount_whenSameApprovalStatus() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum highListenersAlbum = createAlbum(1L, "Album1", "mbid1", ApprovalStatus.APPROVED, 2000);
        LastfmAlbum lowListenersAlbum = createAlbum(2L, "Album2", "mbid2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(highListenersAlbum, lowListenersAlbum), 
            "Album with higher listeners count should have higher priority");
        assertFalse(generator.hasHigherPriority(lowListenersAlbum, highListenersAlbum), 
            "Album with lower listeners count should not have higher priority");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByArtistListenersCount_whenSameAlbumListenersCount() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist popularArtist = createArtist(1L, "Popular Artist");
        popularArtist.setListenersCount(5000);
        
        LastfmArtist lessPopularArtist = createArtist(2L, "Less Popular Artist");
        lessPopularArtist.setListenersCount(1000);
        
        LastfmAlbum albumWithPopularArtist = createAlbum(1L, "Album1", "mbid1", ApprovalStatus.APPROVED, 1000);
        albumWithPopularArtist.setArtist(popularArtist);
        
        LastfmAlbum albumWithLessPopularArtist = createAlbum(2L, "Album2", "mbid2", ApprovalStatus.APPROVED, 1000);
        albumWithLessPopularArtist.setArtist(lessPopularArtist);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(albumWithPopularArtist, albumWithLessPopularArtist), 
            "Album with more popular artist should have higher priority");
        assertFalse(generator.hasHigherPriority(albumWithLessPopularArtist, albumWithPopularArtist), 
            "Album with less popular artist should not have higher priority");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByIdWhenEverythingElseIsEqual() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum olderAlbum = createAlbum(1L, "Album1", "mbid1", ApprovalStatus.APPROVED, 1000);
        LastfmAlbum newerAlbum = createAlbum(2L, "Album2", "mbid2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(olderAlbum, newerAlbum), 
            "Album with lower ID (older) should have higher priority");
        assertFalse(generator.hasHigherPriority(newerAlbum, olderAlbum), 
            "Album with higher ID (newer) should not have higher priority");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenAlbumHasMbid() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum album = createAlbum(1L, "Album1", "mbid1", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(album), 
            "Album with MBID should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenAlbumHasNameAndArtist() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum album = createAlbum(1L, "Album1", null, ApprovalStatus.APPROVED, 1000);
        LastfmArtist artist = createArtist(1L, "Artist1");
        album.setArtist(artist);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(album), 
            "Album with name and artist should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenAlbumHasNoMbidAndNoArtist() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum album = createAlbum(1L, "Album1", null, ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertFalse(generator.isValidForApiCall(album), 
            "Album without MBID and artist should not be valid for API call");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnMbidKey_whenAlbumHasMbid() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum album = createAlbum(1L, "Album1", "mbid1", ApprovalStatus.APPROVED, 1000);
        
        // When
        String key = generator.getApiCallUniqueKey(album);
        
        // Then
        assertEquals("mbid-mbid1", key, "Key should be based on MBID");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNameKey_whenAlbumHasNoMbidButHasArtist() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum album = createAlbum(1L, "Album1", null, ApprovalStatus.APPROVED, 1000);
        LastfmArtist artist = createArtist(1L, "Artist1");
        album.setArtist(artist);
        
        // When
        String key = generator.getApiCallUniqueKey(album);
        
        // Then
        assertEquals("names-Artist1-Album1", key, "Key should be based on artist name and album name");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenAlbumIsInvalid() {
        // Given
        generator = new TestableLastfmAlbumApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmAlbum album = createAlbum(1L, "Album1", null, ApprovalStatus.APPROVED, 1000);
        // No artist set, so album is invalid
        
        // When
        String key = generator.getApiCallUniqueKey(album);
        
        // Then
        assertNull(key, "Key should be null for invalid album");
    }

    // open protected methods for testing
    private static class TestableLastfmAlbumApiCallGenerator extends LastfmAlbumApiCallGenerator {
        public TestableLastfmAlbumApiCallGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmApiCallEntityService entityService
        ) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public List<LastfmAlbum> deduplicateEntitiesForApiCalls(List<LastfmAlbum> entities) {
            return super.deduplicateEntitiesForApiCalls(entities);
        }

        @Override
        public List<LastfmAlbum> selectEntitiesForApiCalls() {
            return super.selectEntitiesForApiCalls();
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ALBUM_GET_INFO;
        }

        @Override
        protected int getDueDurationDays() {
            return 28;
        }
        
        @Override
        public boolean isValidForApiCall(LastfmAlbum album) {
            return super.isValidForApiCall(album);
        }
        
        @Override
        public String getApiCallUniqueKey(LastfmAlbum album) {
            return super.getApiCallUniqueKey(album);
        }
        
        @Override
        public boolean hasHigherPriority(LastfmAlbum candidate, LastfmAlbum existing) {
            return super.hasHigherPriority(candidate, existing);
        }
    }

    private LastfmAlbum createAlbum(Long id, String name, String mbid, ApprovalStatus status, Integer listenersCount) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ALBUM_GET_INFO)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(Map.of())
            .build();
            
        LastfmAlbum album = LastfmAlbum.builder()
            .name(name)
            .mbid(mbid)
            .approvalStatus(status)
            .listenersCount(listenersCount)
            .apiCall(mockApiCall)
            .build();
        
        ReflectionTestUtils.setField(album, "id", id);
        return album;
    }
    
    private LastfmArtist createArtist(Long id, String name) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ARTIST_GET_INFO)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(Map.of())
            .build();
            
        LastfmArtist artist = LastfmArtist.builder()
            .name(name)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(mockApiCall)
            .build();
        
        ReflectionTestUtils.setField(artist, "id", id);
        return artist;
    }
}
