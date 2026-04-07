package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo.TrackGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.test.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.EntityTextContent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmTrackTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmAlbumTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmTrackTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmAlbumTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmTrackTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.BaseLastfmApiResponseProcessorTest;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.TestEntityTextContentRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.getinfo.LastfmTrackGetInfoAlbumFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.getinfo.LastfmTrackGetInfoArtistFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.getinfo.LastfmTrackGetInfoTagFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    // processing
    LastfmTrackGetInfoResponseProcessor.class,
    LastfmTrackGetInfoArtistFactory.class,
    LastfmTrackGetInfoAlbumFactory.class,
    LastfmTrackGetInfoTagFactory.class,
    // entities
    LastfmTrackServiceImpl.class,
    LastfmArtistServiceImpl.class,
    LastfmAlbumServiceImpl.class,
    LastfmTagServiceImpl.class,
    // relations
    LastfmAlbumTrackServiceImpl.class,
    LastfmTrackTagServiceImpl.class,
    LastfmArtistTrackServiceImpl.class,
})
class LastfmTrackGetInfoResponseProcessorTest extends BaseLastfmApiResponseProcessorTest {

    @Autowired
    private LastfmTrackGetInfoResponseProcessor processor;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private TestLastfmAlbumTrackRepository albumTrackRepository;
    
    @Autowired
    private TestLastfmArtistTrackRepository artistTrackRepository;

    @Autowired
    private TestLastfmTrackTagRepository trackTagRepository;

    @Autowired
    private TestEntityTextContentRepository textContentRepository;

    private static final String TEST_RESPONSE_KEY = "track.getInfo";
    private String responseJsonString;
    private TrackGetInfoDtoRoot dtoRoot;

    @BeforeEach
    public void setUp() throws IOException {
        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private TrackGetInfoDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, TrackGetInfoDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse() {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.TRACK_GET_INFO);
    }

    /**
     * Helper method to create API response for testing with a specific entity
     */
    private LastfmApiResponse createApiResponse(LastfmTrack track) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.TRACK_GET_INFO, track);
    }

    @Test
    void process_shouldCreateNewRecords_whenTrackGetInfoResponseAndNoExistingTrackProvided() throws Exception {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify track was created
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        assertEquals(1, savedTracks.size(), "Track should be saved to database");

        LastfmTrack track = savedTracks.get(0);
        assertEquals(dtoRoot.getTrack().getName(), track.getName(), "Track name should match");
        assertEquals(dtoRoot.getTrack().getMbid(), track.getMbid(), "Track MBID should match");
        assertEquals(dtoRoot.getTrack().getUrl(), track.getUrl(), "Track URL should match");
        assertEquals(dtoRoot.getTrack().getDuration(), track.getDuration(), "Track duration should match");

        // Verify artist was created
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        assertEquals(1, savedArtists.size(), "Artist should be saved to database");

        LastfmArtist artist = savedArtists.get(0);
        assertEquals(dtoRoot.getTrack().getArtist().getName(), artist.getName(), "Artist name should match");

        // Verify artist is set on track
        assertNotNull(track.getArtist(), "Track should have artist reference");
        assertEquals(artist.getId(), track.getArtist().getId(), "Track artist ID should match created artist ID");

        // Verify artist-track relationship was created
        List<LastfmArtistTrack> artistTracks = artistTrackRepository.findAll();
        assertFalse(artistTracks.isEmpty(), "Artist-track relationship should be created");
        assertEquals(artist.getId(), artistTracks.get(0).getArtist().getId(), "Artist ID should match");
        assertEquals(track.getId(), artistTracks.get(0).getTrack().getId(), "Track ID should match");

        // Verify album was created if present
        if (dtoRoot.getTrack().getAlbum() != null) {
            List<LastfmAlbum> savedAlbums = albumRepository.findAll();
            assertFalse(savedAlbums.isEmpty(), "Album should be saved to database");

            LastfmAlbum album = savedAlbums.get(0);
            assertEquals(dtoRoot.getTrack().getAlbum().getName(), album.getName(), "Album name should match");

            // Verify album-track relationship was created
            List<LastfmAlbumTrack> albumTracks = albumTrackRepository.findAll();
            assertFalse(albumTracks.isEmpty(), "Album-track relationship should be created");
            assertEquals(album.getId(), albumTracks.get(0).getAlbum().getId(), "Album ID should match");
            assertEquals(track.getId(), albumTracks.get(0).getTrack().getId(), "Track ID should match");

            // Verify position is set if available
            if (dtoRoot.getTrack().getAlbum().getPosition() != null) {
                assertEquals(dtoRoot.getTrack().getAlbum().getPosition(), albumTracks.get(0).getPosition(),
                    "Album track position should match");
            }
        }

        // Verify tags were created
        int expectedTagsCount = dtoRoot.getTrack().getTopTags().getTags().size();
        List<LastfmTag> savedTags = tagRepository.findAll();
        assertEquals(expectedTagsCount, savedTags.size(), "All tags should be saved to database");

        // Verify track-tag relationships were created
        List<LastfmTrackTag> trackTags = trackTagRepository.findAll();
        assertEquals(expectedTagsCount, trackTags.size(), "Track-tag relationships should be created");

        // Verify wiki text content was saved
        List<EntityTextContent> textContents = textContentRepository.findByEntityTypeAndEntityId(
                LastfmEntityType.TRACK, track.getId());
        assertEquals(2, textContents.size(), "Both wiki summary and wiki content should be saved");
        assertTrue(textContents.stream().anyMatch(tc -> tc.getContentType() == TextContentType.WIKI_SUMMARY),
                "Wiki summary should be saved");
        assertTrue(textContents.stream().anyMatch(tc -> tc.getContentType() == TextContentType.WIKI_CONTENT),
                "Wiki content should be saved");
    }

    // ========== Blacklist validation tests ==========

    @Test
    void process_shouldSkipProcessing_whenArtistIsBlacklisted() throws Exception {
        // given
        // Blacklist the artist from the response
        String artistUrl = dtoRoot.getTrack().getArtist().getUrl();
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artistUrl);

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();

        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        long initialAlbumCount = albumRepository.count();
        long initialTagCount = tagRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify no entities were created due to artist blacklist
        assertEquals(initialTrackCount, trackRepository.count(), "No tracks should be created");
        assertEquals(initialArtistCount, artistRepository.count(), "No artists should be created");
        assertEquals(initialAlbumCount, albumRepository.count(), "No albums should be created");
        assertEquals(initialTagCount, tagRepository.count(), "No tags should be created");

        // Verify no relationships were created
        assertEquals(0, artistTrackRepository.count(), "No artist-track relationships should be created");
        assertEquals(0, albumTrackRepository.count(), "No album-track relationships should be created");
        assertEquals(0, trackTagRepository.count(), "No track-tag relationships should be created");
    }

    @Test
    void process_shouldSkipAlbumProcessing_whenAlbumIsBlacklisted() throws Exception {
        // given
        // Blacklist the album from the response (if present)
        if (dtoRoot.getTrack().getAlbum() != null) {
            String albumUrl = dtoRoot.getTrack().getAlbum().getUrl();
            blacklistService.addToBlacklist(LastfmEntityType.ALBUM, albumUrl);

            // Create API response
            LastfmApiResponse apiResponse = createApiResponse();

            // Record initial state
            long initialAlbumCount = albumRepository.count();
            long initialAlbumTrackCount = albumTrackRepository.count();

            // when
            processor.processResponse(apiResponse);

            // then
            // Verify track and artist were still created
            assertEquals(1, trackRepository.count(), "Track should be created");
            assertEquals(1, artistRepository.count(), "Artist should be created");

            // Verify album was not created due to blacklist
            assertEquals(initialAlbumCount, albumRepository.count(), "No albums should be created");

            // Verify no album-track relationship was created
            assertEquals(initialAlbumTrackCount, albumTrackRepository.count(), 
                "No album-track relationships should be created");

            // Verify other relationships were still created
            assertEquals(1, artistTrackRepository.count(), "Artist-track relationship should be created");
            assertTrue(trackTagRepository.count() > 0, "Track-tag relationships should be created");
        }
    }

    @Test
    void process_shouldFilterBlacklistedTags() throws Exception {
        // given
        // Blacklist some tags from the response
        var tagUrls = dtoRoot.getTrack().getTopTags().getTags().stream()
            .map(tag -> tag.getUrl())
            .toList();

        // Blacklist the first tag (if there are any)
        if (!tagUrls.isEmpty()) {
            blacklistService.addToBlacklist(LastfmEntityType.TAG, tagUrls.get(0));

            // Create API response
            LastfmApiResponse apiResponse = createApiResponse();

            // Record initial state
            long initialTagCount = tagRepository.count();
            long initialTrackTagCount = trackTagRepository.count();

            // when
            processor.processResponse(apiResponse);

            // then
            // Verify track and artist were still created
            assertEquals(1, trackRepository.count(), "Track should be created");
            assertEquals(1, artistRepository.count(), "Artist should be created");

            // Verify not all tags were created due to blacklist
            int expectedTagsCount = dtoRoot.getTrack().getTopTags().getTags().size();
            assertTrue(tagRepository.count() < expectedTagsCount, 
                "Some tags should be filtered out due to blacklist");

            // Verify not all track-tag relationships were created
            assertTrue(trackTagRepository.count() < expectedTagsCount, 
                "Some track-tag relationships should be filtered out");

            // Verify other relationships were still created
            assertEquals(1, artistTrackRepository.count(), "Artist-track relationship should be created");
        }
    }

    @Test
    void process_shouldProcessNormally_whenNoEntitiesAreBlacklisted() throws Exception {
        // given
        // Create API response (no blacklisting)
        LastfmApiResponse apiResponse = createApiResponse();

        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        long initialAlbumCount = albumRepository.count();
        long initialTagCount = tagRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify track was created
        assertEquals(1, trackRepository.count() - initialTrackCount, "Track should be created");

        // Verify artist was created
        assertEquals(1, artistRepository.count() - initialArtistCount, "Artist should be created");

        // Verify album was created if present in response
        if (dtoRoot.getTrack().getAlbum() != null) {
            assertTrue(albumRepository.count() > initialAlbumCount, "Album should be created");
            assertTrue(albumTrackRepository.count() > 0, "Album-track relationship should be created");
        }

        // Verify tags were created
        int expectedTagsCount = dtoRoot.getTrack().getTopTags().getTags().size();
        assertEquals(expectedTagsCount, tagRepository.count() - initialTagCount, 
            "All tags should be created");

        // Verify relationships were created
        assertEquals(1, artistTrackRepository.count(), "Artist-track relationship should be created");
        assertEquals(expectedTagsCount, trackTagRepository.count(), 
            "Track-tag relationships should be created");
    }

    @Test
    void process_shouldHandleEmptyTagsAfterBlacklist_whenAllTagsAreBlacklisted() throws Exception {
        // given
        // Blacklist ALL tags from the response
        dtoRoot.getTrack().getTopTags().getTags().stream()
            .map(tag -> tag.getUrl())
            .forEach(tagUrl -> blacklistService.addToBlacklist(LastfmEntityType.TAG, tagUrl));

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();

        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialTrackTagCount = trackTagRepository.count();

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify track and artist were still created
        assertEquals(1, trackRepository.count(), "Track should be created");
        assertEquals(1, artistRepository.count(), "Artist should be created");

        // Verify no tags were created due to blacklist
        assertEquals(initialTagCount, tagRepository.count(), "No tags should be created");

        // Verify no track-tag relationships were created
        assertEquals(initialTrackTagCount, trackTagRepository.count(), 
            "No track-tag relationships should be created");

        // Verify other relationships were still created
        assertEquals(1, artistTrackRepository.count(), "Artist-track relationship should be created");
    }
}
