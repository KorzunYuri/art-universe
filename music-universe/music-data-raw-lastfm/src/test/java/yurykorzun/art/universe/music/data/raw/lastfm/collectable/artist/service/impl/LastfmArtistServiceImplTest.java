package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Import(LastfmArtistServiceImpl.class)
class LastfmArtistServiceImplTest extends JpaOnlyTest {
    
    @MockitoBean
    private LastfmArtistRepository artistRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistServiceImpl artistService;

    @Test
    void givenArtist_whenSaveArtist_thenRepositorySaveIsCalled() {
        LastfmArtist artist = consistencyHelper.createArtist();
        when(artistRepository.save(artist)).thenReturn(artist);

        LastfmArtist savedArtist = artistService.saveArtist(artist);

        assertNotNull(savedArtist);
        assertEquals(artist, savedArtist);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void givenArtists_whenSaveArtists_thenRepositorySaveAllIsCalled() {
        List<LastfmArtist> artists = List.of(consistencyHelper.createArtist(), consistencyHelper.createArtist());
        when(artistRepository.saveAll(artists)).thenReturn(artists);

        List<LastfmArtist> savedArtists = artistService.saveArtists(artists);

        assertNotNull(savedArtists);
        assertEquals(artists.size(), savedArtists.size());
        assertEquals(artists, savedArtists);
        verify(artistRepository, times(1)).saveAll(artists);
    }

    @Test
    void givenUrls_whenFindAllByUrls_thenReturnMatchingArtists() {
        final int artistsNumber = 3;
        List<String> urls = IntStream.range(0, artistsNumber).mapToObj(i -> UUID.randomUUID().toString()).toList();
        List<LastfmArtist> artists = urls.stream()
            .map(url -> consistencyHelper.createArtist(builder -> builder.url(url)))
            .toList();
        when(artistRepository.findAllByNameIn(urls)).thenReturn(artists);

        List<LastfmArtist> foundArtists = artistService.findAllByNames(urls);

        assertNotNull(foundArtists);
        assertEquals(artists.size(), foundArtists.size());
        assertEquals(artists, foundArtists);
        verify(artistRepository, times(1)).findAllByNameIn(urls);
    }
}