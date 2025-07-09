package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
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
    LastfmArtistTopTagsResponseProcessor.class,
    LastfmArtistTopTagsTagFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmTagServiceImpl.class,
    LastfmArtistServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmArtistTagServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
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

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistTopTagsResponseProvided() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTags");
        ArtistTopTagsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopTagsObject().getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_TAGS, sourceArtist);
        
        // Set threshold to 0 to process all tags
        ReflectionTestUtils.setField(processor, "tagUsageCountThreshold", 0);
        
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
        
        // Verify tag properties
        List<LastfmTag> savedTags = tagRepository.findAll();
        for (LastfmTag tag : savedTags) {
            assertNotNull(tag.getName(), "Tag name should be set");
            assertNotNull(tag.getUrl(), "Tag URL should be set");
        }
        
        // Verify relation properties
        List<LastfmArtistTag> relations = artistTagRepository.findAll();
        for (LastfmArtistTag relation : relations) {
            assertEquals(sourceArtist.getId(), relation.getArtist().getId(), 
                "Relation should reference the source artist");
            assertNotNull(relation.getTag(), "Relation should reference a tag");
            assertNotNull(relation.getUsageCount(), "Relation should have usage count set");
        }
    }

    @Test
    void process_shouldFilterTagsByUsageCount_whenThresholdIsSet() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTags");
        ArtistTopTagsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopTagsObject().getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_TAGS, sourceArtist);
        
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
    void process_shouldThrowException_whenSourceArtistNotFound() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTags");
        
        // Create source artist first (needed for API call creation)
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response with the artist
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_TAGS, sourceArtist);
        
        // Now delete the artist to simulate non-existent artist
        artistRepository.delete(sourceArtist);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source artist not found");
    }

    private ArtistTopTagsDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistTopTagsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
