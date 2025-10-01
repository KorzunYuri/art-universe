package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.config.LastfmThresholdConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TestTaskCoordinatorConfig;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        // processing
        LastfmArtistTopTracksResponseProcessor.class,
        LastfmArtistTopTracksArtistFactory.class,
        LastfmApiDtoProcessingService.class,
        // quality control
        BlacklistedEntityUrlService.class,
        DtoQualityService.class,
        LastfmThresholdConfig.class,
        // entities
        LastfmTrackServiceImpl.class,
        LastfmArtistServiceImpl.class,
        // attributes
        LastfmAttributeHistoryServiceImpl.class,
        LastfmAttributeTypeSynchronizer.class,
        LastfmAttributeHistoryProcessor.class,
        TestTaskCoordinatorConfig.class,
        // relations
        LastfmArtistTrackServiceImpl.class,
})
class LastfmArtistTopTracksResponseProcessorTest extends JpaTestWithHelper {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopTracksResponseProcessor processor;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    @Autowired
    private LastfmArtistTrackRepository artistTrackRepository;
    
    private static final String TEST_RESPONSE_KEY = "artist.getTopTracks";
    private String responseJsonString;
    private ArtistTopTracksDtoRoot dtoRoot;

    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
        
        // Set threshold to 0 to process all tracks by default

    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private ArtistTopTracksDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, ArtistTopTracksDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(LastfmArtist artist) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ARTIST_TOP_TRACKS, artist);
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistTopTracksResponseProvided() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getRootObject().getArtistMetadata().getArtistName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new tracks were created
        int expectedTracksCount = dtoRoot.getRootObject().getTracks().size();
        assertEquals(initialTrackCount + expectedTracksCount, trackRepository.count(), 
            "New tracks should be created");

        // Verify artist-track relations were created
        assertTrue(artistTrackRepository.count() > initialArtistTrackCount, 
            "Artist-track relations should be created");
        
        // Verify track properties and attributes
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        for (LastfmTrack track : savedTracks) {
            assertNotNull(track.getName(), "Track name should be set");
            assertNotNull(track.getUrl(), "Track URL should be set");
        }
        
        // Verify relation properties
        List<LastfmArtistTrack> relations = artistTrackRepository.findAll();
        for (LastfmArtistTrack relation : relations) {
            assertNotNull(relation.getArtist(), "Relation should reference an artist");
            assertNotNull(relation.getTrack(), "Relation should reference a track");
            
            // Find the track in the saved tracks
            LastfmTrack track = relation.getTrack();
            
            // Find corresponding track DTO
            var trackDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getName().equals(track.getName()) && 
                       dto.getUrl().equals(track.getUrl()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Track doesn't correspond to any DTO"));
            
            // Verify that the artist in the relation matches the artist in the track DTO
            if (trackDto.getArtist() != null) {
                assertEquals(trackDto.getArtist().getName(), relation.getArtist().getName(),
                    "Artist in relation should match artist in track metadata");
            }
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getRootObject().getArtistMetadata().getArtistName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
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
    void process_shouldHandleEmptyTracksList() throws IOException {
        // given
        // Create empty tracks response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
        ObjectNode topTracksNode = (ObjectNode) jsonNode.path("toptracks");
        topTracksNode.putArray("track"); // Replace tracks with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response with empty tracks
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.ARTIST_TOP_TRACKS, sourceArtist);
        
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
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create invalid API response
        String invalidJson = "{\"invalid\": true}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.ARTIST_TOP_TRACKS, sourceArtist);
        
        // when/then
        Exception exception = assertThrows(UnrecognizedPropertyException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, trackRepository.count(), "No tracks should be created");
    }

    @Test
    void process_shouldSkipBlacklistedTracks_whenSomeTracksAreBlacklisted() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(UUID.randomUUID().toString()) // different artist from the one from response, to test artist creation
        );
        
        // Blacklist some tracks from the response
        var tracks = dtoRoot.getRootObject().getTracks();
        blacklistService.addToBlacklist(LastfmEntityType.TRACK, tracks.getFirst().getUrl());

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify some but not all tracks were created
        long finalTrackCount = trackRepository.count();
        assertTrue(finalTrackCount > initialTrackCount, "Some tracks should be created");
        assertTrue(finalTrackCount < initialTrackCount + tracks.size(), 
            "Not all tracks should be created due to blacklist");

        // Verify artists and relationships were created
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be created");
        assertTrue(artistTrackRepository.count() > 0, "Artist-track relationships should be created");
    }

    @Test
    void process_shouldSkipBlacklistedArtists_whenSomeArtistsAreBlacklisted() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getRootObject().getArtistMetadata().getArtistName())
        );
        
        // Blacklist the artist from tracks (all tracks have same artist in test data)
        var tracks = dtoRoot.getRootObject().getTracks();
        if (!tracks.isEmpty()) {
            blacklistService.addToBlacklist(LastfmEntityType.ARTIST, tracks.getFirst().getArtist().getUrl());
        }

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify no tracks were created due to blacklisted artist
        assertEquals(initialTrackCount, trackRepository.count(), "No tracks should be created");
        assertEquals(initialArtistCount, artistRepository.count(), "No new artists should be created");
        assertEquals(0, artistTrackRepository.count(), "No artist-track relationships should be created");
    }

    @Test
    void process_shouldProcessNormally_whenNoEntitiesAreBlacklisted() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(UUID.randomUUID().toString()) // different artist from the one from response, to test artist creation
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify tracks and artists were created (those passing DtoQualityService validation)
        assertTrue(trackRepository.count() > initialTrackCount, "Tracks should be created");
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be created");
        assertTrue(artistTrackRepository.count() > 0, "Artist-track relationships should be created");
    }
}