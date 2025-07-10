package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsDtoRoot;
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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmArtistTopTagsResponseProcessor.class,
    LastfmArtistTopTagsTagFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmTagServiceImpl.class,
    LastfmArtistServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmArtistTagServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmArtistTopTagsResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopTagsResponseProcessor processor;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistTagRepository artistTagRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "artist.getTopTags";
    private String responseJsonString;
    private ArtistTopTagsDtoRoot dtoRoot;
    private static final int DEFAULT_THRESHOLD = 0;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
        
        // Set threshold to 0 to process all tags by default
        ReflectionTestUtils.setField(processor, "tagUsageCountThreshold", DEFAULT_THRESHOLD);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private ArtistTopTagsDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, ArtistTopTagsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(LastfmArtist artist) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ARTIST_TOP_TAGS, artist);
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistTopTagsResponseProvided() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopTagsObject().getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTagCount = artistTagRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new tags were created
        int expectedTagsCount = dtoRoot.getTopTagsObject().getTags().size();
        assertEquals(initialTagCount + expectedTagsCount, tagRepository.count(), 
            "New tags should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-tag relations were created
        assertEquals(initialArtistTagCount + expectedTagsCount, artistTagRepository.count(), 
            "Artist-tag relations should be created");
        
        // Verify tag properties and attributes
        List<LastfmTag> savedTags = tagRepository.findAll();
        for (LastfmTag tag : savedTags) {
            assertNotNull(tag.getName(), "Tag name should be set");
            assertNotNull(tag.getUrl(), "Tag URL should be set");
            
            // Find corresponding tag in the DTO to get the original values
            var tagDto = dtoRoot.getTopTagsObject().getTags().stream()
                .filter(dto -> dto.getName().equals(tag.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Saved tag doesn't correspond to any DTO"));

            // Verify attributes using the test helper
            testHelper.verifyStringAttribute(tag, LastfmAttribute.URL, tagDto.getUrl());
        }
        
        // Verify relation properties
        List<LastfmArtistTag> relations = artistTagRepository.findAll();
        for (LastfmArtistTag relation : relations) {
            assertEquals(sourceArtist.getId(), relation.getArtist().getId(), 
                "Relation should reference the source artist");
            assertNotNull(relation.getTag(), "Relation should reference a tag");
            assertNotNull(relation.getUsageCount(), "Relation should have usage count set");
            
            // Find corresponding tag in the DTO to get the original usage count
            var tagDto = dtoRoot.getTopTagsObject().getTags().stream()
                .filter(dto -> dto.getName().equals(relation.getTag().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Saved tag doesn't correspond to any DTO"));

            assertEquals(tagDto.getUsageCount(), relation.getUsageCount(),
                "Relation usage count should match DTO value");
        }
    }

    @Test
    void process_shouldFilterTagsByUsageCount_whenThresholdIsSet() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopTagsObject().getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Set high threshold to filter some tags
        int threshold = 100; // Set threshold to filter out some tags
        ReflectionTestUtils.setField(processor, "tagUsageCountThreshold", threshold);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        
        // Count how many tags should pass the threshold
        long expectedTagsCount = dtoRoot.getTopTagsObject().getTags().stream()
            .filter(tag -> tag.getUsageCount() >= threshold)
            .count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only tags above threshold were processed
        assertEquals(initialTagCount + expectedTagsCount, tagRepository.count(), 
            "Only tags above threshold should be created");
        
        // Verify all created relations have usage count above threshold
        List<LastfmArtistTag> relations = artistTagRepository.findAll();
        for (LastfmArtistTag relation : relations) {
            assertTrue(relation.getUsageCount() >= threshold, 
                "All created relations should have usage count above threshold");
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopTagsObject().getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // First processing
        processor.processResponse(apiResponse);
        
        // Record state after first processing
        long tagCountAfterFirstProcessing = tagRepository.count();
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        long relationCountAfterFirstProcessing = artistTagRepository.count();
        
        // when - process the same response again
        processor.processResponse(apiResponse);
        
        // then - counts should remain the same
        assertEquals(tagCountAfterFirstProcessing, tagRepository.count(),
            "Tag count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
        assertEquals(relationCountAfterFirstProcessing, artistTagRepository.count(),
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
    void process_shouldHandleEmptyTagsList() throws IOException {
        // given
        // Create empty tags response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
        ObjectNode topTagsNode = (ObjectNode) jsonNode.path("toptags");
        topTagsNode.putArray("tag"); // Replace tags with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response with empty tags
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.ARTIST_TOP_TAGS, sourceArtist);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialRelationCount = artistTagRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then - no new entities should be created
        assertEquals(initialTagCount, tagRepository.count(),
            "No new tags should be created for empty response");
        assertEquals(initialRelationCount, artistTagRepository.count(),
            "No new relations should be created for empty response");
    }
    
    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create invalid API response
        String invalidJson = "{\"toptags\": {\"invalid\": true}}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.ARTIST_TOP_TAGS, sourceArtist);
        
        // when/then
        assertThrows(RuntimeException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, tagRepository.count(), "No tags should be created");
    }
}
