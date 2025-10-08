package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        DbConsistencyHelper.class,
})
class LastfmTrackRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;

    private LastfmArtist artist;
    private List<LastfmTrack> testTracks;

    @BeforeEach
    void setUp() {
        artist = consistencyHelper.createAndSaveArtist(builder -> builder.approvalStatus(ApprovalStatus.APPROVED));
        
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
                .apiCall(apiCall)
            .build();
        LastfmTrack saved = trackRepository.save(track);

        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(duration, saved.getDuration());
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
}
