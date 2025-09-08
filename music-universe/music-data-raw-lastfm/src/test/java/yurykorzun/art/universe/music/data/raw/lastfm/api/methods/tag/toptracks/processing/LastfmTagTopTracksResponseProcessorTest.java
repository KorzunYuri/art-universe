package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.config.LastfmThresholdConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TestTaskCoordinatorConfig;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        // processing
        LastfmTagTopTracksResponseProcessor.class,
        LastfmTagTopTracksArtistFactory.class,
        LastfmApiDtoProcessingService.class,
        // quality control
        BlacklistedEntityUrlService.class,
        DtoQualityService.class,
        LastfmThresholdConfig.class,
        // entities
        LastfmArtistServiceImpl.class,
        LastfmTrackServiceImpl.class,
        LastfmTagServiceImpl.class,
        // attributes
        LastfmAttributeHistoryServiceImpl.class,
        LastfmAttributeTypeSynchronizer.class,
        LastfmAttributeHistoryProcessor.class,
        TestTaskCoordinatorConfig.class,
        // relations
        LastfmArtistTrackServiceImpl.class,
})
class LastfmTagTopTracksResponseProcessorTest extends JpaOnlyTest {
    
    @Autowired
    private DbConsistencyHelper consistencyHelper;
    
    @Autowired
    private LastfmTagTopTracksResponseProcessor processor;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistTrackRepository artistTrackRepository;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;
    
    private static final String TEST_RESPONSE_KEY = "tag.getTopTracks";
    private String responseJsonString;
    private TagTopTracksDtoRoot dtoRoot;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();

        // Parse test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private TagTopTracksDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, TagTopTracksDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(String responseString, LastfmTag tag) {
        return consistencyHelper.createAndSaveApiResponse(responseString, LastfmApiCallType.TAG_TOP_TRACKS, tag);
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopTracksResponseProvided() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString, sourceTag);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new tracks were created
        int expectedTracksCount = dtoRoot.getRootObject().getTracks().size();
        assertEquals(initialTrackCount + expectedTracksCount, trackRepository.count(), 
            "New tracks should be created");
        
        // Verify new artists were created (should be 2 unique artists)
        int expectedArtistsCount = 27;
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Verify attribute history records were created
        assertFalse(attributeHistoryRepository.count() > initialAttributeCount,
            "New attribute history records should not be created");
        
        // Verify artist-track relations were created
        assertEquals(initialArtistTrackCount + expectedTracksCount, artistTrackRepository.count(), 
            "Artist-track relations should be created");
        
        // Verify track properties and attributes
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        for (LastfmTrack track : savedTracks) {
            assertNotNull(track.getName(), "Track name should be set");
            assertNotNull(track.getUrl(), "Track URL should be set");
            
            // Find corresponding track in the DTO to get the original values
            var trackDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getName().equals(track.getName()) && 
                       dto.getUrl().equals(track.getUrl()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Saved track doesn't correspond to any DTO"));

            assertEquals(trackDto.getUrl(), track.getUrl());
            assertEquals(trackDto.getDuration(), track.getDuration());
            if (trackDto.getMbid() != null && !trackDto.getMbid().isEmpty()) {
                assertEquals(trackDto.getMbid(), track.getMbid());
            }
        }
        
        // Verify artist properties and attributes
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            assertNotNull(artist.getUrl(), "Artist URL should be set");
            
            // Find corresponding artist in the DTO to get the original values
            var artistDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getArtist().getName().equals(artist.getName()))
                .findFirst()
                .map(dto -> dto.getArtist())
                .orElse(null);
                
            if (artistDto != null) {
                assertEquals(artistDto.getUrl(), artist.getUrl());
                if (artistDto.getMbid() != null && !artistDto.getMbid().isEmpty()) {
                    assertEquals(artistDto.getMbid(), artist.getMbid());
                }
            }
        }
        
        // Verify relation properties
        List<LastfmArtistTrack> relations = artistTrackRepository.findAll();
        for (LastfmArtistTrack relation : relations) {
            assertNotNull(relation.getArtist(), "Relation should reference an artist");
            assertNotNull(relation.getTrack(), "Relation should reference a track");
            
            // Verify the artist-track relationship is correct
            var trackDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getName().equals(relation.getTrack().getName()))
                .findFirst()
                .orElse(null);
                
            if (trackDto != null) {
                assertEquals(trackDto.getArtist().getName(), relation.getArtist().getName(),
                    "Track should be associated with the correct artist");
            }
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString, sourceTag);
        
        // First processing
        processor.processResponse(apiResponse);
        
        // Record state after first processing
        long trackCountAfterFirstProcessing = trackRepository.count();
        long artistCountAfterFirstProcessing = artistRepository.count();
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        long relationCountAfterFirstProcessing = artistTrackRepository.count();
        
        // when - process the same response again
        processor.processResponse(apiResponse);
        
        // then - counts should remain the same
        assertEquals(trackCountAfterFirstProcessing, trackRepository.count(),
            "Track count should remain the same after second processing");
        assertEquals(artistCountAfterFirstProcessing, artistRepository.count(),
            "Artist count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
        assertEquals(relationCountAfterFirstProcessing, artistTrackRepository.count(),
            "Relation count should remain the same after second processing");
    }

    @Test
    void process_shouldThrowException_whenSourceTagNotFound() throws IOException {
        // given
        // Create source tag first (needed for API call creation)
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with the tag
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString, sourceTag);
        
        // Now delete the tag to simulate non-existent tag
        tagRepository.delete(sourceTag);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source tag not found");
    }
    
    @Test
    void process_shouldHandleEmptyTracksList() throws IOException {
        // given
        // Create empty tracks response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
        ObjectNode tracksNode = (ObjectNode) jsonNode.path("tracks");
        tracksNode.putArray("track"); // Replace tracks with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);
        
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with empty tracks
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        long initialRelationCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then - no new entities should be created
        assertEquals(initialTrackCount, trackRepository.count(),
            "No new tracks should be created for empty response");
        assertEquals(initialArtistCount, artistRepository.count(),
            "No new artists should be created for empty response");
        assertEquals(initialRelationCount, artistTrackRepository.count(),
            "No new relations should be created for empty response");
    }
    
    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create invalid API response
        String invalidJson = "{\"invalid\": true}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);
        
        // when/then
        assertThrows(UnrecognizedPropertyException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(1, tagRepository.count(), "Only source tag should exist");
        assertEquals(0, trackRepository.count(), "No tracks should be created");
        assertEquals(0, artistRepository.count(), "No artists should be created");
    }
    
    @Test
    void process_shouldHandleArtistDeduplication() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString, sourceTag);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify Nirvana artist was created only once despite having two tracks
        List<LastfmArtist> nirvanaArtists = artistRepository.findAll().stream()
            .filter(artist -> artist.getName().equals("Nirvana"))
            .toList();
            
        assertEquals(1, nirvanaArtists.size(), 
            "Artist should be deduplicated when appearing multiple times");
            
        // Verify both Nirvana tracks are associated with the same artist
        LastfmArtist nirvanaArtist = nirvanaArtists.getFirst();
        List<LastfmArtistTrack> nirvanaRelations = artistTrackRepository.findAll().stream()
            .filter(relation -> relation.getArtist().getId() == nirvanaArtist.getId())
            .toList();
            
        assertEquals(5, nirvanaRelations.size(),
            "Both tracks should be associated with the same artist");
    }

    @Test
    void process_shouldSkipBlacklistedArtists_whenSomeArtistsAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Blacklist some artists from the response
        var tracks = dtoRoot.getRootObject().getTracks();
        if (tracks.size() > 1) {
            // Blacklist the first artist (Nirvana)
            blacklistService.addToBlacklist(
                LastfmEntityType.ARTIST,
                tracks.get(0).getArtist().getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify some but not all artists were created
        long finalArtistCount = artistRepository.count();
        assertTrue(finalArtistCount > initialArtistCount, "Some artists should be created");
        
        // Count unique artists in response
        long uniqueArtists = tracks.stream()
            .map(track -> track.getArtist().getName())
            .distinct()
            .count();
        assertTrue(finalArtistCount < initialArtistCount + uniqueArtists, 
            "Not all artists should be created due to blacklist");

        // Verify tracks from non-blacklisted artists were created
        long finalTrackCount = trackRepository.count();
        assertTrue(finalTrackCount > initialTrackCount, "Some tracks should be created");
        
        // But tracks from blacklisted artists should not be created
        assertTrue(finalTrackCount < initialTrackCount + tracks.size(), 
            "Not all tracks should be created due to blacklisted artists");

        // Verify relationships were created
        assertTrue(artistTrackRepository.count() > 0, "Artist-track relationships should be created");
    }

    @Test
    void process_shouldSkipBlacklistedTracks_whenSomeTracksAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Blacklist some tracks from the response
        var tracks = dtoRoot.getRootObject().getTracks();
        if (tracks.size() > 1) {
            // Blacklist the first track (Smells Like Teen Spirit)
            blacklistService.addToBlacklist(
                LastfmEntityType.TRACK,
                tracks.get(0).getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify artists were created (not affected by track blacklist)
        long finalArtistCount = artistRepository.count();
        assertTrue(finalArtistCount > initialArtistCount, "Artists should be created");

        // Verify some but not all tracks were created
        long finalTrackCount = trackRepository.count();
        assertTrue(finalTrackCount > initialTrackCount, "Some tracks should be created");
        assertTrue(finalTrackCount < initialTrackCount + tracks.size(), 
            "Not all tracks should be created due to blacklist");

        // Verify relationships were created
        assertTrue(artistTrackRepository.count() > 0, "Artist-track relationships should be created");
    }

    @Test
    void process_shouldSkipTracksFromBlacklistedArtists_whenArtistIsBlacklistedButTrackIsNot() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        var tracks = dtoRoot.getRootObject().getTracks();
        if (tracks.size() > 1) {
            // Blacklist an artist but not their track
            String artistUrl = tracks.getFirst().getArtist().getUrl();
            blacklistService.addToBlacklist(
                LastfmEntityType.ARTIST,
                artistUrl);
            
            // Verify the track itself is not blacklisted
            String trackUrl = tracks.getFirst().getUrl();
            // Don't blacklist the track - we want to test that tracks are filtered due to blacklisted artists
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);

        // Record initial state
        long initialTrackCount = trackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify tracks from blacklisted artists were not created
        long finalTrackCount = trackRepository.count();
        
        // Should have fewer tracks than total tracks in response
        assertTrue(finalTrackCount < initialTrackCount + tracks.size(), 
            "Tracks from blacklisted artists should not be created");
    }

    @Test
    void process_shouldHandleAllArtistsBlacklisted_whenAllArtistsAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Blacklist ALL unique artists from the response
        var tracks = dtoRoot.getRootObject().getTracks();
        var uniqueArtistUrls = tracks.stream()
            .map(track -> track.getArtist().getUrl())
            .distinct()
            .toList();
            
        for (String artistUrl : uniqueArtistUrls) {
            blacklistService.addToBlacklist(
                LastfmEntityType.ARTIST,
                artistUrl);
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify no artists, tracks or relationships were created
        assertEquals(1, tagRepository.count(), "Only source tag should exist");
        assertEquals(0, artistRepository.count(), "No artists should be created");
        assertEquals(0, trackRepository.count(), "No tracks should be created");
        assertEquals(0, artistTrackRepository.count(), "No artist-track relationships should be created");
    }

    @Test
    void process_shouldProcessNormally_whenNoEntitiesAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify artists and tracks were created
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be created");
        assertTrue(trackRepository.count() > initialTrackCount, "Tracks should be created");
        assertTrue(artistTrackRepository.count() > 0, "Artist-track relationships should be created");
        assertFalse(attributeHistoryRepository.count() > 0, "Attribute history records should not be created");
    }

    @Test
    void processResponse_shouldDeduplicateArtistsByName_whenMultipleTracksHaveSameArtist() throws IOException {
        // Given
        String responseJson = """
            {
              "tracks": {
                "track": [
                  {
                    "name": "Feeling Sorry",
                    "url": "https://www.last.fm/music/Paramore/_/Feeling+Sorry",
                    "artist": {
                      "name": "Paramore",
                      "mbid": "44cf61b8-5197-448a-b82b-cef6ee89fac5",
                      "url": "https://www.last.fm/music/Paramore"
                    },
                    "@attr": {"rank": "41"}
                  },
                  {
                    "name": "Hallelujah",
                    "url": "https://www.last.fm/music/Paramore/_/Hallelujah",
                    "artist": {
                      "name": "Paramore",
                      "mbid": "44cf61b8-5197-448a-b82b-cef6ee89fac5",
                      "url": "https://www.last.fm/music/Paramore"
                    },
                    "@attr": {"rank": "49"}
                  },
                  {
                    "name": "Born for This",
                    "url": "https://www.last.fm/music/Paramore/_/Born+for+This",
                    "artist": {
                      "name": "Paramore",
                      "mbid": "44cf61b8-5197-448a-b82b-cef6ee89fac5",
                      "url": "https://www.last.fm/music/Paramore"
                    },
                    "@attr": {"rank": "50"}
                  },
                  {
                    "name": "Chasing Cars",
                    "url": "https://www.last.fm/music/Snow+Patrol/_/Chasing+Cars",
                    "artist": {
                      "name": "Snow Patrol",
                      "mbid": "a66999a7-ae5c-460e-ba94-1a01143ae847",
                      "url": "https://www.last.fm/music/Snow+Patrol"
                    },
                    "@attr": {"rank": "1"}
                  }
                ]
              }
            }
            """;

        LastfmTag sourceTag = consistencyHelper.createAndSaveTag(builder ->
            builder.name("rock")
        );
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder ->
            builder .type(LastfmApiCallType.TAG_TOP_TRACKS)
                    .entityId(sourceTag.getId())
        );
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(responseJson, sourceApiCall);


        // When
        processor.processResponse(apiResponse);

        // Then
        assertEquals(2, artistRepository.count(), "Artists should be deduplicated");
    }

    @Test
    void processResponse_shouldPreserveSingleArtistInstance_whenNoDeduplicationNeeded() throws IOException {
        // Given
        String responseJson = """
            {
              "tracks": {
                "track": [
                  {
                    "name": "Chasing Cars",
                    "url": "https://www.last.fm/music/Snow+Patrol/_/Chasing+Cars",
                    "artist": {
                      "name": "Snow Patrol",
                      "mbid": "a66999a7-ae5c-460e-ba94-1a01143ae847",
                      "url": "https://www.last.fm/music/Snow+Patrol"
                    },
                    "@attr": {"rank": "1"}
                  },
                  {
                    "name": "Hells Bells",
                    "url": "https://www.last.fm/music/AC%2FDC/_/Hells+Bells",
                    "artist": {
                      "name": "AC/DC",
                      "mbid": "66c662b6-6e2f-4930-8610-912e24c63ed1",
                      "url": "https://www.last.fm/music/AC%2FDC"
                    },
                    "@attr": {"rank": "3"}
                  }
                ]
              }
            }
            """;

        LastfmTag sourceTag = consistencyHelper.createAndSaveTag(builder ->
            builder.name("rock")
        );
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder ->
            builder .type(LastfmApiCallType.TAG_TOP_TRACKS)
                .entityId(sourceTag.getId())
        );
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(responseJson, sourceApiCall);

        // When
        processor.processResponse(apiResponse);

        // Then
        assertEquals(2, artistRepository.count(), "Should have exactly 2 artists when no deduplication needed");

        List<String> artistNames = artistRepository.findAll().stream()
            .map(LastfmArtist::getName)
            .sorted()
            .toList();

        assertEquals(List.of("AC/DC", "Snow Patrol"), artistNames,
            "Should contain AC/DC and Snow Patrol");
    }
}