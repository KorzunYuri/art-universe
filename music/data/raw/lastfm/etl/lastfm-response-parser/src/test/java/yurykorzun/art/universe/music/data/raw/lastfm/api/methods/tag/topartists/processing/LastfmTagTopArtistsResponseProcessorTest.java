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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.TestLastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.TestLastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseLastfmApiResponseProcessorTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmTagTopArtistsResponseProcessor.class,
    LastfmTagTopArtistsArtistFactory.class,
    // entities
    LastfmArtistServiceImpl.class,
    LastfmTagServiceImpl.class,
    // relations
    LastfmArtistTagServiceImpl.class,
})
class LastfmTagTopArtistsResponseProcessorTest extends BaseLastfmApiResponseProcessorTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopArtistsResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private TestLastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    @Autowired
    private TestLastfmArtistTagRepository artistTagRepository;

    private static final String TEST_RESPONSE_KEY = "tag.getTopArtists";
    private String responseJsonString;
    private TagTopArtistsDtoRoot dtoRoot;
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
    private LastfmApiResponse createApiResponse(String responseString, LastfmTag tag) {
        return consistencyHelper.createAndSaveApiResponse(responseString, LastfmApiCallType.TAG_TOP_ARTISTS, tag);
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopArtistsResponseProvided() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString, sourceTag);
        
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
        assertFalse(attributeHistoryRepository.count() > initialAttributeCount,
            "New attribute history records should not be created");
        
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
            assertEquals(artistDto.getUrl(), artist.getUrl());
            if (artistDto.getMbid() != null && !artistDto.getMbid().isEmpty()) {
                assertEquals(artistDto.getMbid(), artist.getMbid());
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
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString,sourceTag);
        
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
        LastfmApiResponse apiResponse = createApiResponse(responseJsonString, sourceTag);
        
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
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
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

    @Test
    void process_shouldSkipBlacklistedArtists_whenSomeArtistsAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Blacklist some artists from the response
        var artists = dtoRoot.getTopArtists().getArtists();
        // Blacklist the first artist
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artists.getFirst().getUrl());

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);

        // Record initial state
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify some but not all artists were created
        long finalArtistCount = artistRepository.count();
        assertTrue(finalArtistCount > initialArtistCount, "Some artists should be created");
        assertTrue(finalArtistCount < initialArtistCount + artists.size(), 
            "Not all artists should be created due to blacklist");

        // Verify relationships were created
        assertTrue(artistTagRepository.count() > 0, "Artist-tag relationships should be created");
        assertFalse(attributeHistoryRepository.count() > 0, "Attribute history records should not be created");
    }

    @Test
    void process_shouldHandleAllArtistsBlacklisted_whenAllArtistsAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Blacklist ALL artists from the response
        TagTopArtistsDtoRoot dtoFromRealResponse = parseResponse(responseJsonString);
        var artists = dtoFromRealResponse.getTopArtists().getArtists();
        for (var artist : artists) {
            blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artist.getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify no artists or relationships were created
        assertEquals(1, tagRepository.count(), "Only source tag should exist");
        assertEquals(0, artistRepository.count(), "No artists should be created");
        assertEquals(0, artistTagRepository.count(), "No artist-tag relationships should be created");
    }

    @Test
    void process_shouldProcessNormally_whenNoArtistsAreBlacklisted() throws IOException {
        // given
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);

        // Record initial state
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify artists were created
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be created");
        assertTrue(artistTagRepository.count() > 0, "Artist-tag relationships should be created");
        assertFalse(attributeHistoryRepository.count() > 0, "Attribute history records should not be created");
    }
}