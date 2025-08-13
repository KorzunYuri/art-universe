package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Import({
    LastfmEntityServiceImpl.class,
})
class LastfmEntityServiceImplBlacklistTest extends JpaOnlyTest {

    @Autowired
    private LastfmEntityService entityService;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void findAllUnprocessed_shouldExcludeBlacklistedArtists() {
        // Given
        LastfmArtist artist1 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 1")
            .url("https://www.last.fm/music/Artist+1"));
        
        LastfmArtist artist2 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 2")
            .url("https://www.last.fm/music/Artist+2"));
        
        LastfmArtist artist3 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 3")
            .url("https://www.last.fm/music/Artist+3"));

        // Blacklist artist2
        dbHelper.addToBlacklist(LastfmEntityType.ARTIST, artist2.getUrl());

        // make sure changes have been applied
        dbHelper.flush();

        List<LastfmArtist> artists = artistRepository.findAll();

        // When
        List<LastfmArtist> unprocessedArtists = entityService.findAllUnprocessed(
            LastfmEntityType.ARTIST,
            LastfmApiCallType.ARTIST_GET_INFO,
            LastfmEntityQueryConfig.builder()
                .approvedEntitiesOnly(false)  // Include PENDING entities
                .build()
        );

        // Then
        assertThat(unprocessedArtists)
            .hasSize(2)
            .extracting(LastfmArtist::getName)
            .containsExactlyInAnyOrder("Artist 1", "Artist 3");
    }

    @Test
    void findAllUnprocessed_shouldExcludeBlacklistedTracks() {
        // Given
        LastfmTrack track1 = dbHelper.createAndSaveTrack(builder -> builder
            .name("Track 1")
            .url("https://www.last.fm/music/Artist/_/Track+1"));
        
        LastfmTrack track2 = dbHelper.createAndSaveTrack(builder -> builder
            .name("Track 2")
            .url("https://www.last.fm/music/Artist/_/Track+2"));
        
        LastfmTrack track3 = dbHelper.createAndSaveTrack(builder -> builder
            .name("Track 3")
            .url("https://www.last.fm/music/Artist/_/Track+3"));

        // Blacklist track2
        dbHelper.addToBlacklist(LastfmEntityType.TRACK, track2.getUrl());

        // make sure changes have been applied
        dbHelper.flush();

        // When
        List<LastfmTrack> unprocessedTracks = entityService.findAllUnprocessed(
            LastfmEntityType.TRACK, 
            LastfmApiCallType.TRACK_GET_INFO,
            LastfmEntityQueryConfig.builder()
                .approvedEntitiesOnly(false)  // Include PENDING entities
                .build()
        );

        // Then
        assertThat(unprocessedTracks)
            .hasSize(2)
            .extracting(LastfmTrack::getName)
            .containsExactlyInAnyOrder("Track 1", "Track 3");
    }

    @Test
    void findAllUnprocessed_shouldNotExcludeEntitiesWithDifferentEntityType() {
        // Given
        LastfmArtist artist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist")
            .url("https://www.last.fm/music/Test+Artist"));
        
        LastfmTrack track = dbHelper.createAndSaveTrack(builder -> builder
            .name("Test Track")
            .url("https://www.last.fm/music/Test+Artist"));

        // Blacklist the URL for TRACK entity type only
        dbHelper.addToBlacklist(LastfmEntityType.TRACK, "https://www.last.fm/music/Test+Artist");

        // make sure changes have been applied
        dbHelper.flush();

        // When - search for artists with the same URL
        List<LastfmArtist> unprocessedArtists = entityService.findAllUnprocessed(
            LastfmEntityType.ARTIST, 
            LastfmApiCallType.ARTIST_GET_INFO,
            LastfmEntityQueryConfig.builder()
                .approvedEntitiesOnly(false)  // Include PENDING entities
                .build()
        );

        // Then - artist should not be excluded (different entity type)
        assertThat(unprocessedArtists)
            .hasSize(1)
            .extracting(LastfmArtist::getName)
            .containsExactly("Test Artist");

        // When - search for tracks
        List<LastfmTrack> unprocessedTracks = entityService.findAllUnprocessed(
            LastfmEntityType.TRACK, 
            LastfmApiCallType.TRACK_GET_INFO,
            LastfmEntityQueryConfig.builder()
                .approvedEntitiesOnly(false)  // Include PENDING entities
                .build()
        );

        // Then - track should be excluded
        assertThat(unprocessedTracks).isEmpty();
    }

    @Test
    void findAllUnprocessed_shouldReturnAllWhenNoBlacklist() {
        // Given
        LastfmArtist artist1 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 1")
            .url("https://www.last.fm/music/Artist+1"));
        
        LastfmArtist artist2 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 2")
            .url("https://www.last.fm/music/Artist+2"));

        // make sure changes have been applied
        dbHelper.flush();

        // When - no blacklist entries
        List<LastfmArtist> unprocessedArtists = entityService.findAllUnprocessed(
            LastfmEntityType.ARTIST, 
            LastfmApiCallType.ARTIST_GET_INFO,
            LastfmEntityQueryConfig.builder()
                .approvedEntitiesOnly(false)  // Include PENDING entities
                .build()
        );

        // Then
        assertThat(unprocessedArtists)
            .hasSize(2)
            .extracting(LastfmArtist::getName)
            .containsExactlyInAnyOrder("Artist 1", "Artist 2");
    }

    @Test
    void findAllUnprocessed_shouldHandleNullUrls() {
        // Given
        LastfmArtist artistWithUrl = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist With URL")
            .url("https://www.last.fm/music/Artist+With+URL"));
        
        LastfmArtist artistWithNullUrl = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist With Null URL")
            .url(null));

        // Blacklist the first artist
        dbHelper.addToBlacklist(LastfmEntityType.ARTIST, artistWithUrl.getUrl());

        // make sure changes have been applied
        dbHelper.flush();

        // When
        List<LastfmArtist> unprocessedArtists = entityService.findAllUnprocessed(
            LastfmEntityType.ARTIST, 
            LastfmApiCallType.ARTIST_GET_INFO,
            LastfmEntityQueryConfig.builder()
                .approvedEntitiesOnly(false)  // Include PENDING entities
                .build()
        );

        // Then - only artist with null URL should be returned
        assertThat(unprocessedArtists)
            .hasSize(1)
            .extracting(LastfmArtist::getName)
            .containsExactly("Artist With Null URL");
    }
}
