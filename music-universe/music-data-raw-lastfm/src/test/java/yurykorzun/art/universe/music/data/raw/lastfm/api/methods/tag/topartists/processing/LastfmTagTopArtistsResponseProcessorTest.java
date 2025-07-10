package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsDtoRoot;
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
    LastfmTagTopArtistsResponseProcessor.class,
    LastfmTagTopArtistsArtistFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmArtistServiceImpl.class,
    LastfmTagServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmArtistTagServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmTagTopArtistsResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopArtistsResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistTagRepository artistTagRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private TagTopArtistsDtoRoot dtoRoot;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Parse test data once for all tests
        dtoRoot = parseResponse(TEST_DTO_ROOT);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private TagTopArtistsDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, TagTopArtistsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(LastfmTag tag) {
        return consistencyHelper.createAndSaveApiResponse(TEST_DTO_ROOT, LastfmApiCallType.TAG_TOP_ARTISTS, tag);
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopArtistsResponseProvided() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTagCount = artistTagRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new artists were created
        int expectedArtistsCount = dtoRoot.getTopArtists().getArtists().size();
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-tag relations were created
        assertEquals(initialArtistTagCount + expectedArtistsCount, artistTagRepository.count(), 
            "Artist-tag relations should be created");
        
        // Verify artist properties and attributes
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            assertNotNull(artist.getUrl(), "Artist URL should be set");
            
            // Find corresponding artist in the DTO to get the original values
            var artistDto = dtoRoot.getTopArtists().getArtists().stream()
                .filter(dto -> dto.getName().equals(artist.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Saved artist doesn't correspond to any DTO"));

            // Verify attributes using the test helper
            testHelper.verifyStringAttribute(artist, LastfmAttribute.URL, artistDto.getUrl());

            if (artistDto.getMbid() != null && !artistDto.getMbid().isEmpty()) {
                testHelper.verifyStringAttribute(artist, LastfmAttribute.MBID, artistDto.getMbid());
            }
        }
        
        // Verify relation properties
        List<LastfmArtistTag> relations = artistTagRepository.findAll();
        for (LastfmArtistTag relation : relations) {
            assertEquals(sourceTag.getId(), relation.getTag().getId(), 
                "Relation should reference the source tag");
            assertNotNull(relation.getArtist(), "Relation should reference an artist");
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // First processing
        processor.processResponse(apiResponse);
        
        // Record state after first processing
        long artistCountAfterFirstProcessing = artistRepository.count();
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        long relationCountAfterFirstProcessing = artistTagRepository.count();
        
        // when - process the same response again
        processor.processResponse(apiResponse);
        
        // then - counts should remain the same
        assertEquals(artistCountAfterFirstProcessing, artistRepository.count(),
            "Artist count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
        assertEquals(relationCountAfterFirstProcessing, artistTagRepository.count(),
            "Relation count should remain the same after second processing");
    }

    @Test
    void process_shouldThrowException_whenSourceTagNotFound() throws IOException {
        // given
        // Create source tag first (needed for API call creation)
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with the tag
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // Now delete the tag to simulate non-existent tag
        tagRepository.delete(sourceTag);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source tag not found");
    }
    
    @Test
    void process_shouldHandleEmptyArtistsList() throws IOException {
        // given
        // Create empty artists response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(TEST_DTO_ROOT);
        ObjectNode topArtistsNode = (ObjectNode) jsonNode.path("topartists");
        topArtistsNode.putArray("artist"); // Replace artists with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);
        
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with empty artists
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialRelationCount = artistTagRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then - no new entities should be created
        assertEquals(initialArtistCount, artistRepository.count(),
            "No new artists should be created for empty response");
        assertEquals(initialRelationCount, artistTagRepository.count(),
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
            invalidJson, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);
        
        // when/then
        Exception exception = assertThrows(UnrecognizedPropertyException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(1, tagRepository.count(), "Only source tag should exist");
        assertEquals(0, artistRepository.count(), "No artists should be created");
    }

    // "image" array is ignored but is left here to make it realistic
    private static final String TEST_DTO_ROOT = """
        {
          "topartists": {
            "artist": [
              {
                "name": "Coldplay",
                "mbid": "cc197bad-dc9c-440d-a5b5-d52ba2e14234",
                "url": "https://www.last.fm/music/Coldplay",
                "streamable": "0",
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "mega"
                  }
                ],
                "@attr": {
                  "rank": "1"
                }
              },
              {
                "name": "Linkin Park",
                "mbid": "f59c5520-5f46-4d2c-b2c4-822eabf53419",
                "url": "https://www.last.fm/music/Linkin+Park",
                "streamable": "0",
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "mega"
                  }
                ],
                "@attr": {
                  "rank": "2"
                }
              }
            ],
            "@attr": {
              "tag": "rock",
              "page": "1",
              "perPage": "50",
              "totalPages": "3578",
              "total": "178853"
            }
          }
        }
        """;
}
