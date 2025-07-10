package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmTrackRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;
    
    private LastfmArtist artist;
    private List<LastfmTrack> testTracks;

    @BeforeEach
    void setUp() {
        artist = consistencyHelper.createAndSaveArtist();
        
        // Create test tracks with different values for sorting tests
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        
        testTracks = new ArrayList<>();
        
        // Track with all values set
        testTracks.add(LastfmTrack.builder()
            .name("Track A")
            .url("https://example.com/track-a")
            .mbid("mbid-a")
            .playCount(1000L)
            .listenersCount(500)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Track with null playCount
        testTracks.add(LastfmTrack.builder()
            .name("Track B")
            .url("https://example.com/track-b")
            .mbid("mbid-b")
            .playCount(null)
            .listenersCount(800)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Track with null listenersCount
        testTracks.add(LastfmTrack.builder()
            .name("Track C")
            .url("https://example.com/track-c")
            .mbid("mbid-c")
            .playCount(1500L)
            .listenersCount(null)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Track with both null
        testTracks.add(LastfmTrack.builder()
            .name("Track D")
            .url("https://example.com/track-d")
            .mbid("mbid-d")
            .playCount(null)
            .listenersCount(null)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Save all test tracks
        trackRepository.saveAll(testTracks);
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    private LastfmTrack createTrack() {
        return consistencyHelper.createAndSaveTrack();
    }

    @Test
    void save_shouldSaveTrack_whenValidDataProvided() {
        final String name = "Smells Like Teen Spirit";
        final int duration = 301;
        final String url = "https://www.last.fm/music/Nirvana/_/Smells+Like+Teen+Spirit";
        final boolean streamable = true;
        final String mbid = "0ebe2d92-a11d-4b2b-9922-806383074ed7";

        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmTrack track = LastfmTrack.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
                .duration(duration)
                .isStreamable(streamable)
                .apiCall(apiCall)
            .build();
        LastfmTrack saved = trackRepository.save(track);

        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(duration, saved.getDuration());
        assertEquals(streamable, saved.getIsStreamable());
    }

    @Test
    void saveAll_shouldSaveAllTracks_whenValidDataProvided() {
        LastfmTrack track1 = createTrack();
        LastfmTrack track2 = createTrack();

        List<LastfmTrack> firstSaveResult = trackRepository.saveAll(List.of(track1, track2));
        assertEquals(6, trackRepository.findAll().size()); // 4 from setup + 2 new ones
        LastfmTrack track1After1stSave = firstSaveResult.stream()
                .filter(a -> a.getName().equals(track1.getName()))
                .findFirst().get();

        LastfmTrack track3 = createTrack();

        List<LastfmTrack> secondSaveResult = trackRepository.saveAll(List.of(track1, track3));
        assertEquals(7, trackRepository.findAll().size()); // 6 previous + 1 new
        LastfmTrack track1After2ndSave = secondSaveResult.stream()
                .filter(a -> a.getName().equals(track1.getName()))
                .findFirst().get();
        assertEquals(track1After2ndSave.getId(), track1After1stSave.getId());
    }

    @Test
    void findAllByUrlIn_shouldFindAllTracks_whenUrlsProvided() {
        final int totalTracks = 5;
        final int tracksToRetrieve = 2;
        List<String> urls = IntStream.range(0, totalTracks).mapToObj(i -> UUID.randomUUID().toString()).toList();

        List<LastfmTrack> savedTracks = new ArrayList<>();
        for (String url : urls) {
            savedTracks.add(consistencyHelper.createAndSaveTrack(url));
        }
        
        List<String> urlsSubset = urls.subList(0, tracksToRetrieve);
        List<LastfmTrack> retrieved = trackRepository.findAllByUrlIn(urlsSubset);

        assertEquals(tracksToRetrieve, retrieved.size());
        urlsSubset.forEach(url ->
            assertTrue(retrieved.stream().anyMatch(t -> url.equals(t.getUrl()))));
    }
    
    @Test
    void testSortByListenersCount_shouldPlaceNullsLast() {
        // Sort by listenersCount in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "listenersCount"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Track B (800) should be first, then Track A (500), then Track C and D (null)
        assertEquals("Track B", sortedTracks.get(0).getName());
        assertEquals("Track A", sortedTracks.get(1).getName());
        
        // The last two should have null listenersCount
        assertNull(sortedTracks.get(2).getListenersCount());
        assertNull(sortedTracks.get(3).getListenersCount());
    }
    
    @Test
    void testSortByPlayCount_shouldPlaceNullsLast() {
        // Sort by playCount in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "playCount"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Track C (1500) should be first, then Track A (1000), then Track B and D (null)
        assertEquals("Track C", sortedTracks.get(0).getName());
        assertEquals("Track A", sortedTracks.get(1).getName());
        
        // The last two should have null playCount
        assertNull(sortedTracks.get(2).getPlayCount());
        assertNull(sortedTracks.get(3).getPlayCount());
    }
    
    @Test
    void testSortByName_shouldSortAlphabetically() {
        // Sort by name in ascending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Tracks should be in alphabetical order: A, B, C, D
        assertEquals("Track A", sortedTracks.get(0).getName());
        assertEquals("Track B", sortedTracks.get(1).getName());
        assertEquals("Track C", sortedTracks.get(2).getName());
        assertEquals("Track D", sortedTracks.get(3).getName());
    }
    
    @Test
    void testSortByNameDescending_shouldSortReverseAlphabetically() {
        // Sort by name in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Tracks should be in reverse alphabetical order: D, C, B, A
        assertEquals("Track D", sortedTracks.get(0).getName());
        assertEquals("Track C", sortedTracks.get(1).getName());
        assertEquals("Track B", sortedTracks.get(2).getName());
        assertEquals("Track A", sortedTracks.get(3).getName());
    }
}