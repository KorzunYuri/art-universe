package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmArtistRepository repository;

    @Test
    void testArtistSave() {
        final String name = "Queen";
        final String url = "https://www.last.fm/music/Queen";
        String mbid = "cc197bad-dc9c-440d-a5b5-d52ba2e14234";

        LastfmArtist artist = LastfmArtist.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
            .build();
        LastfmArtist saved = repository.save(artist);

        assertEquals(1L, saved.getId());
        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
    }

    @Test
    void testArtistSaveAll() {
        LastfmArtist artist1 = LastfmArtist.builder().name("Queen").build();
        LastfmArtist artist2 = LastfmArtist.builder().name("Deep purple").build();

        List<LastfmArtist> firstSaveResult = repository.saveAll(List.of(artist1, artist2));
        assertEquals(2, repository.findAll().size());
        LastfmArtist artist1After1stSave = firstSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();

        LastfmArtist artist3 = LastfmArtist.builder().name("Metallica").build();

        List<LastfmArtist> secondSaveResult = repository.saveAll(List.of(artist1, artist3));
        assertEquals(3, repository.findAll().size());
        LastfmArtist artist1After2ndSave = secondSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();
        assertEquals(artist1After2ndSave.getId(), artist1After1stSave.getId());
    }
}