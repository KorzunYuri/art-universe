package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.TestLastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseLastfmApiResponseProcessorTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    // processing
    LastfmTagTopTagsResponseProcessor.class,
    LastfmTagTopTagsTagFactory.class,
    // entities
    LastfmTagServiceImpl.class,
})
class LastfmTagTopTagsResponseProcessorTest extends BaseLastfmApiResponseProcessorTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopTagsResponseProcessor processor;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private TestLastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    private static final String TEST_RESPONSE_KEY = "tag.getTopTags";
    private String responseJsonString;
    private TagTopTagsDtoRoot dtoRoot;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();

        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        // Load test data once for all tests
        dtoRoot = parseResponse(responseJsonString);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private TagTopTagsDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, TagTopTagsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse() {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.TAG_TOP_TAGS);
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopTagsResponseProvided() throws IOException {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new tags were created
        int expectedTagsCount = dtoRoot.getTopTags().getTags().size();
        assertEquals(initialTagCount + expectedTagsCount, tagRepository.count(), 
            "New tags should be created");

        // Verify tag properties and attributes
        List<LastfmTag> savedTags = tagRepository.findAll();
        for (LastfmTag tag : savedTags) {
            assertNotNull(tag.getName(), "Tag name should be set");
        }
    }

    @Test
    void process_shouldUpdateExistingTags_whenTagsAlreadyExist() throws IOException {
        // given
        // Create some existing tags first
        int preExistingTagsCount = 5;
        for (int i = 0; i < preExistingTagsCount; i++) {
            String tagName = dtoRoot.getTopTags().getTags().get(i).getName();
            consistencyHelper.createAndSaveTag(builder -> 
                builder.name(tagName)
            );
        }
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only new tags were created (total - pre-existing)
        int expectedNewTagsCount = dtoRoot.getTopTags().getTags().size() - preExistingTagsCount;
        assertEquals(initialTagCount + expectedNewTagsCount, tagRepository.count(), 
            "Only new tags should be created");

        // Verify attributes for pre-existing tags
        for (int i = 0; i < preExistingTagsCount; i++) {
            var tagDto = dtoRoot.getTopTags().getTags().get(i);
            List<LastfmTag> tags = tagRepository.findAllByNameIn(List.of(tagDto.getName()));
            assertFalse(tags.isEmpty(), "Tag should exist");
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // First processing
        processor.processResponse(apiResponse);
        
        // Record state after first processing
        long tagCountAfterFirstProcessing = tagRepository.count();
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        
        // when - process the same response again
        processor.processResponse(apiResponse);
        
        // then - counts should remain the same
        assertEquals(tagCountAfterFirstProcessing, tagRepository.count(),
            "Tag count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
    }

    @Test
    void process_shouldNotCreateNewRecords_whenEmptyResponseProvided() throws IOException {
        // given
        // Create empty tags response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
        ObjectNode topTagsNode = (ObjectNode) jsonNode.path("toptags");
        topTagsNode.putArray("tag"); // Replace tags with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);
        
        // Create API response with empty tags
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.TAG_TOP_TAGS);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then - no new entities should be created
        assertEquals(initialTagCount, tagRepository.count(),
            "No new tags should be created for empty response");
        assertEquals(initialAttributeCount, attributeHistoryRepository.count(),
            "No new attribute records should be created for empty response");
    }
    
    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        // Create invalid API response
        String invalidJson = "{\"invalid\": true}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.TAG_TOP_TAGS);
        
        // when/then
        Exception exception = assertThrows(UnrecognizedPropertyException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(0, tagRepository.count(), "No tags should be created");
    }
}
