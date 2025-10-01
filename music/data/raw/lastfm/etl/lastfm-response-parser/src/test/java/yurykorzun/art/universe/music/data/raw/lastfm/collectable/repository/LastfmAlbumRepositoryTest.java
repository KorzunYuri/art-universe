package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        DbConsistencyHelper.class,
})
class LastfmAlbumRepositoryTest extends JpaTestWithHelper {

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        // Clean up before each test to ensure consistent state
        consistencyHelper.cleanup();
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    @Test
    void save_shouldSaveAlbum_whenValidDataProvided() {
        final String name = "album";
        final String description = "description";
        final String url = "url";
        final String mbid = "mbid";
        final long playCount = 1;
        final int listenersCount = 1;
        final LocalDateTime publishTs = LocalDateTime.now();

        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmAlbum album = LastfmAlbum.builder()
                .apiCall(apiCall)
                .name(name)
                .description(description)
                .url(url)
                .mbid(mbid)
                .playCount(playCount)
                .listenersCount(listenersCount)
                .publishTs(publishTs)
            .build();

        LastfmAlbum saved = albumRepository.save(album);

        assertEquals(name, saved.getName());
        assertEquals(description, saved.getDescription());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(playCount, saved.getPlayCount());
        assertEquals(listenersCount, saved.getListenersCount());
        assertEquals(publishTs, saved.getPublishTs());
    }

    @Test
    void save_shouldSaveAlbums_whenValidDataProvided() {
        LastfmAlbum album1 = consistencyHelper.createAlbumForPersistence();
        LastfmAlbum album2 = consistencyHelper.createAlbumForPersistence();

        List<LastfmAlbum> firstSaveResult = albumRepository.saveAll(List.of(album1, album2));
        assertEquals(2, firstSaveResult.size());
        assertEquals(2, albumRepository.findAll().size());
        LastfmAlbum album1after1stSave = firstSaveResult.stream()
            .filter(a -> album1.getName().equals(a.getName()))
            .findFirst().get();

        LastfmAlbum album3 = consistencyHelper.createAlbumForPersistence();
        List<LastfmAlbum> secondSaveResult = albumRepository.saveAll(List.of(album1, album3));
        assertEquals(2, secondSaveResult.size());
        assertEquals(3, albumRepository.findAll().size());
        LastfmAlbum album1after2ndSave = firstSaveResult.stream()
            .filter(a -> album1.getName().equals(a.getName()))
            .findFirst().get();
        assertEquals(album1after1stSave.getId(), album1after2ndSave.getId());
    }
    

}
