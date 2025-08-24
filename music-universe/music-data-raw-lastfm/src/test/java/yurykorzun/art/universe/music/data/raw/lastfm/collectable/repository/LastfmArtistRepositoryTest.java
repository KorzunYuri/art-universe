package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;
    
    private LastfmArtist radiohead;
    private LastfmArtist radioMoscow;
    private LastfmArtist metallica;
    
    @BeforeEach
    void setup() {
        // Create test data
        radiohead = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Radiohead")
            .playCount(10000L)
            .listenersCount(5000)
            .approvalStatus(ApprovalStatus.APPROVED));
            
        radioMoscow = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Radio Moscow")
            .playCount(5000L)
            .listenersCount(2000)
            .approvalStatus(ApprovalStatus.PENDING));
            
        metallica = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Metallica")
            .playCount(20000L)
            .listenersCount(10000)
            .approvalStatus(ApprovalStatus.APPROVED));
    }
    
    @AfterEach
    void cleanup() {
        consistencyHelper.cleanup();
    }

    @Test
    void save_shouldSaveArtist_whenValidDataProvided() {
        final String name = "Queen";
        final String url = "https://www.last.fm/music/Queen";
        final String mbid = "cc197bad-dc9c-440d-a5b5-d52ba2e14234";

        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmArtist artist = LastfmArtist.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
                .apiCall(apiCall)
            .build();
        LastfmArtist saved = artistRepository.save(artist);

        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
    }

    @Test
    void saveAll_shouldSaveAllArtists_whenValidDataProvided() {
        LastfmArtist artist1 = consistencyHelper.createAndSaveArtist(builder -> builder.name("Queen"));
        LastfmArtist artist2 = consistencyHelper.createAndSaveArtist(builder -> builder.name("Deep purple"));

        List<LastfmArtist> firstSaveResult = artistRepository.saveAll(List.of(artist1, artist2));
        assertEquals(5, artistRepository.findAll().size()); // 3 from setup + 2 new ones
        LastfmArtist artist1After1stSave = firstSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();

        LastfmArtist artist3 = consistencyHelper.createAndSaveArtist(builder -> builder.name("Metallica2"));

        List<LastfmArtist> secondSaveResult = artistRepository.saveAll(List.of(artist1, artist3));
        assertEquals(6, artistRepository.findAll().size()); // 5 previous + 1 new
        LastfmArtist artist1After2ndSave = secondSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();
        assertEquals(artist1After2ndSave.getId(), artist1After1stSave.getId());
    }
    
    @Test
    void findArtists_withSearchParam_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            "radio", null, null, null, null, pageable);
            
        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
        assertFalse(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
    }
    
    @Test
    void findArtists_withMinPlayCount_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            null, 10000L, null, null, null, pageable);
            
        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
        assertFalse(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
    }
    
    @Test
    void findArtists_withMinListenersCount_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            null, null, 5000L, null, null, pageable);
            
        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
        assertFalse(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
    }
    
    @Test
    void findArtists_withApprovalStatus_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<ApprovalStatus> approvalStatuses = List.of(ApprovalStatus.APPROVED);
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            null, null, null, approvalStatuses, null, pageable);
            
        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
        assertFalse(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
    }
    
    @Test
    void findArtists_withMultipleParams_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<ApprovalStatus> approvalStatuses = List.of(ApprovalStatus.APPROVED);
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            "radio", 10000L, null, approvalStatuses, null, pageable);
            
        // Then
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertFalse(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
        assertFalse(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
    }
    
    @Test
    void findArtists_withNoParams_shouldReturnAllArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            null, null, null, null, null, pageable);
            
        // Then
        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
    }
    
    @Test
    void findArtists_withPagination_shouldReturnCorrectPage() {
        // Given
        // Add more artists for pagination testing
        consistencyHelper.createAndSaveArtist(builder -> builder.name("AC/DC").approvalStatus(ApprovalStatus.APPROVED));
        consistencyHelper.createAndSaveArtist(builder -> builder.name("Queen").approvalStatus(ApprovalStatus.APPROVED));
        
        // Request first page with size 2, sorted by name
        Pageable firstPageable = PageRequest.of(0, 2, Sort.by("name"));
        
        // When
        Page<LastfmArtist> firstPage = artistRepository.findArtists(
            null, null, null, null, null, firstPageable);
            
        // Then
        assertEquals(5, firstPage.getTotalElements()); // Total of 5 artists
        assertEquals(3, firstPage.getTotalPages()); // 5 artists / 2 per page = 3 pages (rounded up)
        assertEquals(2, firstPage.getContent().size()); // First page has 2 artists
        
        // Check second page
        Pageable secondPageable = PageRequest.of(1, 2, Sort.by("name"));
        Page<LastfmArtist> secondPage = artistRepository.findArtists(
            null, null, null, null, null, secondPageable);
            
        assertEquals(2, secondPage.getContent().size()); // Second page has 2 artists
        
        // Check third page
        Pageable thirdPageable = PageRequest.of(2, 2, Sort.by("name"));
        Page<LastfmArtist> thirdPage = artistRepository.findArtists(
            null, null, null, null, null, thirdPageable);
            
        assertEquals(1, thirdPage.getContent().size()); // Third page has 1 artist
    }
    
    @Test
    void findArtists_withSorting_shouldReturnSortedResults() {
        // Given
        // Sort by listeners count in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "listenersCount"));
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            null, null, null, null, null, pageable);
            
        // Then
        assertEquals(3, result.getTotalElements());
        
        // Check sorting order
        List<LastfmArtist> sortedArtists = result.getContent();
        assertEquals("Metallica", sortedArtists.get(0).getName()); // 10000 listeners
        assertEquals("Radiohead", sortedArtists.get(1).getName()); // 5000 listeners
        assertEquals("Radio Moscow", sortedArtists.get(2).getName()); // 2000 listeners
    }
    
    @Test
    void findArtists_withCaseInsensitiveSearch_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When - search with lowercase
        Page<LastfmArtist> result1 = artistRepository.findArtists(
            "radio", null, null, null, null, pageable);
            
        // Then
        assertEquals(2, result1.getTotalElements());
        
        // When - search with uppercase
        Page<LastfmArtist> result2 = artistRepository.findArtists(
            "RADIO", null, null, null, null, pageable);
            
        // Then
        assertEquals(2, result2.getTotalElements());
        
        // When - search with mixed case
        Page<LastfmArtist> result3 = artistRepository.findArtists(
            "RaDiO", null, null, null, null, pageable);
            
        // Then
        assertEquals(2, result3.getTotalElements());
    }
    
    @Test
    void findArtists_withMultipleApprovalStatuses_shouldReturnMatchingArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<ApprovalStatus> approvalStatuses = List.of(ApprovalStatus.APPROVED, ApprovalStatus.PENDING);
        
        // When
        Page<LastfmArtist> result = artistRepository.findArtists(
            null, null, null, approvalStatuses, null, pageable);
            
        // Then
        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
        assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Metallica")));
    }
    
    @Test
    void findAllByNameIn_shouldReturnMatchingArtists() {
        // Given
        List<String> names = List.of("Radiohead", "Metallica");
        
        // When
        List<LastfmArtist> result = artistRepository.findAllByNameIn(names);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getName().equals("Radiohead")));
        assertTrue(result.stream().anyMatch(a -> a.getName().equals("Metallica")));
        assertFalse(result.stream().anyMatch(a -> a.getName().equals("Radio Moscow")));
    }
    
    @Test
    void findByName_shouldReturnMatchingArtist() {
        // Given
        String name = "Radiohead";
        
        // When
        var result = artistRepository.findByName(name);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Radiohead", result.get().getName());
    }
    
    @Test
    void findByName_withNonExistingName_shouldReturnEmpty() {
        // Given
        String name = "Non Existing Artist";
        
        // When
        var result = artistRepository.findByName(name);
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findAllToGetInfoFor_shouldExcludeBlacklistedArtists() {
        // isolate the test
        artistRepository.deleteAll();

        // Create additional artists
        LastfmArtist artist1 = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Artist 1")
                   .url("https://www.last.fm/music/Artist+1")
                   .listenersCount(null)  // Missing stats - needs getInfo
                   .playCount(null)
                   .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmArtist artist2 = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Artist 2")
                   .url("https://www.last.fm/music/Artist+2")
                   .listenersCount(null)  // Missing stats - needs getInfo
                   .playCount(null)
                   .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmArtist artist3 = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Artist 3")
                   .url("https://www.last.fm/music/Artist+3")
                   .listenersCount(null)  // Missing stats - needs getInfo
                   .playCount(null)
                   .approvalStatus(ApprovalStatus.APPROVED));

        // Add artist2 to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.ARTIST, artist2.getUrl());

        // make sure changes have been applied
        consistencyHelper.flush();

        // Execute query
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor(10);

        // Verify results - should exclude blacklisted artist2
        assertEquals(2, result.size(), "Should return all artists excluding blacklisted");
        assertTrue(result.stream().anyMatch(a -> "Artist 1".equals(a.getName())), 
                  "Should include Artist 1");
        assertTrue(result.stream().anyMatch(a -> "Artist 3".equals(a.getName())), 
                  "Should include Artist 3");
        assertFalse(result.stream().anyMatch(a -> "Artist 2".equals(a.getName())), 
                   "Should exclude blacklisted Artist 2");
    }

    @Test
    void findAllToGetInfoFor_shouldNotExcludeArtistsWithNullUrls() {

        // Create artist with null URL
        LastfmArtist artistWithNullUrl = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Artist With Null URL")
                   .url(null)
                   .listenersCount(null)  // Missing stats - needs getInfo
                   .playCount(null)
                   .approvalStatus(ApprovalStatus.APPROVED));

        // Create artist with empty URL
        LastfmArtist artistWithEmptyUrl = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist With Empty URL")
                   .url("")
                   .listenersCount(null)  // Missing stats - needs getInfo
                   .playCount(null)
                   .approvalStatus(ApprovalStatus.APPROVED));
        
        // Create artist with valid URL and blacklist it
        LastfmArtist artistWithUrl = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Artist With URL but blacklisted")
                   .url("https://www.last.fm/music/Artist+With+URL")
                   .listenersCount(null)  // Missing stats - needs getInfo
                   .playCount(null)
                   .approvalStatus(ApprovalStatus.APPROVED));

        // Add artist with URL to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.ARTIST, artistWithUrl.getUrl());

        // make sure changes have been applied
        consistencyHelper.flush();

        // Execute query
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor(10);

        // Verify results - should include artist with null URL, exclude blacklisted artist
        assertTrue(result.stream().anyMatch(a -> "Artist With Null URL".equals(a.getName())),
            "Should include artist with null URL");
        assertTrue(result.stream().anyMatch(a -> "Artist With Empty URL".equals(a.getName())),
            "Should include artist with empty URL");
        assertFalse(result.stream().anyMatch(a -> "Artist With URL but blacklisted".equals(a.getName())),
            "Should exclude blacklisted artist");
    }
}
