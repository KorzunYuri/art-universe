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
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.impl.LastfmTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmArtistTopTracksResponseProcessor.class,
    LastfmArtistTopTracksTrackFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmTrackServiceImpl.class,
    LastfmArtistServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmArtistTrackServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmArtistTopTracksResponseProcessorTest extends JpaOnlyTest {

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
    private LastfmArtistTrackRepository artistTrackRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "artist.getTopTracks";
    private String responseJsonString;
    private ArtistTopTracksDtoRoot dtoRoot;
    private static final int DEFAULT_THRESHOLD = 0;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
        
        // Set threshold to 0 to process all tracks by default
        ReflectionTestUtils.setField(processor, "trackListenersThreshold", DEFAULT_THRESHOLD);
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
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
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

            // Verify attributes using the test helper
            testHelper.verifyStringAttribute(track, LastfmAttribute.URL, trackDto.getUrl());
            testHelper.verifyIntAttribute(track, LastfmAttribute.LISTENERS_COUNT, trackDto.getListenersCount());
            testHelper.verifyIntAttribute(track, LastfmAttribute.PLAY_COUNT, trackDto.getPlayCount());

            if (trackDto.getMbid() != null && !trackDto.getMbid().isEmpty()) {
                testHelper.verifyStringAttribute(track, LastfmAttribute.MBID, trackDto.getMbid());
            }
        }
        
        // Verify relation properties
        List<LastfmArtistTrack> relations = artistTrackRepository.findAll();
        for (LastfmArtistTrack relation : relations) {
            assertEquals(sourceArtist.getId(), relation.getArtist().getId(), 
                "Relation should reference the source artist");
            assertNotNull(relation.getTrack(), "Relation should reference a track");
        }
    }

    @Test
    void process_shouldFilterTracksByListenersCount_whenThresholdIsSet() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getRootObject().getArtistMetadata().getArtistName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Set high threshold to filter some tracks
        int threshold = 1000000; // High threshold to filter out some tracks
        ReflectionTestUtils.setField(processor, "trackListenersThreshold", threshold);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        
        // Count how many tracks should pass the threshold
        long expectedTracksCount = dtoRoot.getRootObject().getTracks().stream()
            .filter(track -> track.getListenersCount() >= threshold)
            .count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only tracks above threshold were processed
        assertEquals(initialTrackCount + expectedTracksCount, trackRepository.count(), 
            "Only tracks above threshold should be created");
        
        // Verify all created tracks have listeners count above threshold
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        for (LastfmTrack track : savedTracks) {
            // Get listeners count attribute
            List<Integer> listenersCountValues = attributeHistoryRepository.findAttributeValuesForEntity(
                LastfmAttribute.LISTENERS_COUNT, track.getType(), track.getId())
                .stream()
                .map(record -> record.getIntValue())
                .toList();
                
            if (!listenersCountValues.isEmpty()) {
                assertTrue(listenersCountValues.get(0) >= threshold,
                    "Track listeners count should be above threshold");
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
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        long relationCountAfterFirstProcessing = artistTrackRepository.count();
        
        // when - process the same response again
        processor.processResponse(apiResponse);
        
        // then - counts should remain the same
        assertEquals(trackCountAfterFirstProcessing, trackRepository.count(),
            "Track count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
        assertEquals(relationCountAfterFirstProcessing, artistTrackRepository.count(),
            "Relation count should remain the same after second processing");
    }

    @Test
    void process_shouldThrowException_whenSourceArtistNotFound() throws IOException {
        // given
        // Create source artist first (needed for API call creation)
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response with the artist
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Now delete the artist to simulate non-existent artist
        artistRepository.delete(sourceArtist);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source artist not found");
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
        long initialRelationCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then - no new entities should be created
        assertEquals(initialTrackCount, trackRepository.count(),
            "No new tracks should be created for empty response");
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
}
