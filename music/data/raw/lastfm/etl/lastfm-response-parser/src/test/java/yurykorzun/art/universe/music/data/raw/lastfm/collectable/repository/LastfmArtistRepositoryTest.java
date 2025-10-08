package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        DbConsistencyHelper.class,
})
class LastfmArtistRepositoryTest extends LastfmJpaTestHelper {

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
}

