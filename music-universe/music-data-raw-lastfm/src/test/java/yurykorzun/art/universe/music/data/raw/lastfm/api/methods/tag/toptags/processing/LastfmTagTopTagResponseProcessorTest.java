package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
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
    LastfmTagTopTagResponseProcessor.class,
    LastfmTagTopTagsTagFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmTagServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
})
class LastfmTagTopTagResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopTagResponseProcessor processor;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopTagsResponseProvided() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopTags");
        TagTopTagsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.TAG_TOP_TAGS);
        
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
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify tag properties
        List<LastfmTag> savedTags = tagRepository.findAll();
        for (LastfmTag tag : savedTags) {
            assertNotNull(tag.getName(), "Tag name should be set");
        }
        
        // Verify attributes were saved with correct values
        List<LastfmAttributeHistoryRecord> usageCountAttributes = attributeHistoryRepository.findAll().stream()
            .filter(attr -> LastfmAttribute.RELATIONS_COUNT == attr.getAttribute())
            .toList();
        
        assertFalse(usageCountAttributes.isEmpty(), "Usage count attributes should be created");
        
        List<LastfmAttributeHistoryRecord> reachAttributes = attributeHistoryRepository.findAll().stream()
            .filter(attr -> LastfmAttribute.USAGE_COUNT == attr.getAttribute())
            .toList();
        
        assertFalse(reachAttributes.isEmpty(), "Reach attributes should be created");
    }

    @Test
    void process_shouldUpdateExistingTags_whenTagsAlreadyExist() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopTags");
        TagTopTagsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create some existing tags first
        int preExistingTagsCount = 10;
        for (int i = 0; i < preExistingTagsCount; i++) {
            String tagName = dtoRoot.getTopTags().getTags().get(i).getName();
            consistencyHelper.createAndSaveTag(builder -> 
                builder.name(tagName)
            );
        }
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.TAG_TOP_TAGS);
        
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
        
        // Verify attribute history records were created for all tags (new and existing)
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
    }

    @Test
    void process_shouldNotCreateNewRecords_whenEmptyResponseProvided() throws IOException {
        // given
        String emptyResponseString = """
            {
              "toptags": {
                "@attr": {
                  "offset": 0,
                  "num_res": 0,
                  "total": 0
                },
                "tag": []
              }
            }
            """;
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseString, LastfmApiCallType.TAG_TOP_TAGS);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify no new tags were created
        assertEquals(initialTagCount, tagRepository.count(), 
            "No new tags should be created for empty response");
        
        // Verify no new attribute records were created
        assertEquals(initialAttributeCount, attributeHistoryRepository.count(), 
            "No new attribute records should be created for empty response");
    }

    private TagTopTagsDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, TagTopTagsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
