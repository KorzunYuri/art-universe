package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Import(LastfmTrackServiceImpl.class)
class LastfmTrackServiceImplTest extends JpaOnlyTest {

    @MockitoBean
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTrackServiceImpl trackService;

    @Test
    void givenTrack_whenSaveTrack_thenRepositorySaveIsCalled() {
        LastfmTrack track = EntityCreationHelper.createTrack();
        when(trackRepository.save(track)).thenReturn(track);

        LastfmTrack savedTrack = trackService.saveTrack(track);

        assertNotNull(savedTrack);
        assertEquals(track, savedTrack);
        verify(trackRepository, times(1)).save(track);
    }

    @Test
    void givenTracks_whenSaveTracks_thenRepositorySaveAllIsCalled() {
        List<LastfmTrack> tracks = List.of(EntityCreationHelper.createTrack(), EntityCreationHelper.createTrack());
        when(trackRepository.saveAll(tracks)).thenReturn(tracks);

        List<LastfmTrack> savedTracks = trackService.saveTracks(tracks);

        assertNotNull(savedTracks);
        assertEquals(tracks.size(), savedTracks.size());
        assertEquals(tracks, savedTracks);
        verify(trackRepository, times(1)).saveAll(tracks);
    }

    @Test
    void givenUrls_whenFindAllByUrls_thenReturnMatchingTracks() {
        final int tracksNumber = 3;
        List<String> urls = IntStream.range(0, tracksNumber).mapToObj(i -> UUID.randomUUID().toString()).toList();
        List<LastfmTrack> tracks = urls.stream().map(EntityCreationHelper::createTrack).toList();
        when(trackRepository.findAllByUrlIn(urls)).thenReturn(tracks);

        List<LastfmTrack> foundTracks = trackService.findAllByUrls(urls);

        assertNotNull(foundTracks);
        assertEquals(tracks.size(), foundTracks.size());
        assertEquals(tracks, foundTracks);
        verify(trackRepository, times(1)).findAllByUrlIn(urls);
    }

}