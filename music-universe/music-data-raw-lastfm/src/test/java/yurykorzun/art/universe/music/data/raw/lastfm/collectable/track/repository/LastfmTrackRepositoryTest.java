package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTrackRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    private LastfmTrack createTrack(String url, LastfmApiCall apiCall) {
        return LastfmTrack.builder()
                .url(   url)
                .name(  randomString())
                .mbid(  randomString())
                .duration(100)
                .streamable(1)
                .apiCall(apiCall)
            .build();
    }

    private LastfmTrack createTrack(LastfmApiCall apiCall) {
        return createTrack(randomString(), apiCall);
    }

    private LastfmTrack createTrack(String url) {
        return createTrack(url, consistencyHelper.createDummyApiCall());
    }

    private LastfmTrack createTrack() {
        return createTrack(randomString(), consistencyHelper.createDummyApiCall());
    }

    @Test
    void testTrackSave() {
        final String name = "Smells Like Teen Spirit";
        final int duration = 301;
        final String url = "https://www.last.fm/music/Nirvana/_/Smells+Like+Teen+Spirit";
        final int streamable = 1;
        final String mbid = "0ebe2d92-a11d-4b2b-9922-806383074ed7";

        LastfmApiCall apiCall = consistencyHelper.createDummyApiCall();
        LastfmTrack track = LastfmTrack.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
                .duration(duration)
                .streamable(streamable)
                .apiCall(apiCall)
            .build();
        LastfmTrack saved = trackRepository.save(track);

        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(duration, saved.getDuration());
        assertEquals(streamable, saved.getStreamable());
    }

    @Test
    void testTrackSaveAll() {
        LastfmTrack track1 = createTrack();
        LastfmTrack track2 = createTrack();

        List<LastfmTrack> firstSaveResult = trackRepository.saveAll(List.of(track1, track2));
        assertEquals(2, trackRepository.findAll().size());
        LastfmTrack track1After1stSave = firstSaveResult.stream()
            .filter(a -> a.getName().equals(track1.getName()))
            .findFirst().get();

        LastfmTrack track3 = createTrack();

        List<LastfmTrack> secondSaveResult = trackRepository.saveAll(List.of(track1, track3));
        assertEquals(3, trackRepository.findAll().size());
        LastfmTrack track1After2ndSave = secondSaveResult.stream()
            .filter(a -> a.getName().equals(track1.getName()))
            .findFirst().get();
        assertEquals(track1After2ndSave.getId(), track1After1stSave.getId());
    }
    
    @Test
    void testTrackFindAllByUrlIn() {
        final int totalTracks = 5;
        final int tracksToRetrieve = 2;
        List<String> urls = IntStream.range(0, totalTracks).mapToObj(i -> randomString()).toList();

        trackRepository.saveAll(urls.stream().map(this::createTrack).toList());
        List<String> urlsSubset = urls.subList(0, tracksToRetrieve);
        List<LastfmTrack> retrieved = trackRepository.findAllByUrlIn(urlsSubset);

        assertEquals(tracksToRetrieve, retrieved.size());
        urlsSubset.forEach(url ->
            assertTrue(retrieved.stream().anyMatch(t -> urlsSubset.get(0).equals(t.getUrl()))));
    }


}