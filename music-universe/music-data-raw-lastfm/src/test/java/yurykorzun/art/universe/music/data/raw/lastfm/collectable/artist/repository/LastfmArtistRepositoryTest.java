package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    private LastfmDataSnapshot createDummyDataSnapshot() {
        return snapshotRepository.save(new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, new Date()));
    }

    private LastfmApiCall createDummyApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot();
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_ARTISTS)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
                .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    private LastfmArtist createArtist(String name, LastfmApiCall apiCall) {
        return LastfmArtist.builder()
                .name(name)
                .apiCall(apiCall)
                .build();
    }

    @Test
    void testArtistSave() {
        final String name = "Queen";
        final String url = "https://www.last.fm/music/Queen";
        String mbid = "cc197bad-dc9c-440d-a5b5-d52ba2e14234";

        LastfmApiCall apiCall = createDummyApiCall();
        LastfmArtist artist = LastfmArtist.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
                .apiCall(apiCall)
            .build();
        LastfmArtist saved = artistRepository.save(artist);

        assertEquals(1L, saved.getId());
        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
    }

    @Test
    void testArtistSaveAll() {
        LastfmApiCall apiCall = createDummyApiCall();
        LastfmArtist artist1 = createArtist("Queen", apiCall);
        LastfmArtist artist2 = createArtist("Deep purple", apiCall);

        List<LastfmArtist> firstSaveResult = artistRepository.saveAll(List.of(artist1, artist2));
        assertEquals(2, artistRepository.findAll().size());
        LastfmArtist artist1After1stSave = firstSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();

        LastfmArtist artist3 = createArtist("Metallica", apiCall);

        List<LastfmArtist> secondSaveResult = artistRepository.saveAll(List.of(artist1, artist3));
        assertEquals(3, artistRepository.findAll().size());
        LastfmArtist artist1After2ndSave = secondSaveResult.stream()
                .filter(a -> a.getName().equals(artist1.getName()))
                .findFirst().get();
        assertEquals(artist1After2ndSave.getId(), artist1After1stSave.getId());
    }

}