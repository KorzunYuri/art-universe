package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Test
    void testArtistSave() {
        final String name = "Queen";
        final String url = "https://www.last.fm/music/Queen";
        final String mbid = "cc197bad-dc9c-440d-a5b5-d52ba2e14234";
        final boolean isStreamable = true;

        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmArtist artist = LastfmArtist.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
                .apiCall(apiCall)
                .isStreamable(isStreamable)
            .build();
        LastfmArtist saved = artistRepository.save(artist);

        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(isStreamable, saved.getIsStreamable());
    }

    @Test
    void testArtistSaveAll() {
        LastfmArtist artist1 = consistencyHelper.createAndSaveArtist(builder -> builder.name("Queen"));
        LastfmArtist artist2 = consistencyHelper.createAndSaveArtist(builder -> builder.name("Deep purple"));

        List<LastfmArtist> firstSaveResult = artistRepository.saveAll(List.of(artist1, artist2));
        assertEquals(2, artistRepository.findAll().size());
        LastfmArtist artist1After1stSave = firstSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();

        LastfmArtist artist3 = consistencyHelper.createAndSaveArtist(builder -> builder.name("Metallica"));

        List<LastfmArtist> secondSaveResult = artistRepository.saveAll(List.of(artist1, artist3));
        assertEquals(3, artistRepository.findAll().size());
        LastfmArtist artist1After2ndSave = secondSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();
        assertEquals(artist1After2ndSave.getId(), artist1After1stSave.getId());
    }

}