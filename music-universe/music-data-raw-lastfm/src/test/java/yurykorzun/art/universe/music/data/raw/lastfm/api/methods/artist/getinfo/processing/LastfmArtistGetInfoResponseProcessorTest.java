package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.testing.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistsRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmArtistGetInfoResponseProcessor.class,
    LastfmArtistGetInfoArtistFactory.class,
    LastfmArtistGetInfoSimilarArtistFactory.class,
    LastfmArtistGetInfoTagFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmArtistServiceImpl.class,
    LastfmTagServiceImpl.class,
    // relations
    LastfmArtistsRelationServiceImpl.class,
    LastfmArtistTagServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmArtistGetInfoResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistGetInfoResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmArtistTagRepository artistTagRepository;

    @Autowired
    private LastfmArtistsRelationRepository artistsRelationRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "artist.getInfo";
    private String responseJsonString;
    private ArtistGetInfoDtoRoot dtoRoot;

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Load test data once for all tests
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
    private ArtistGetInfoDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistGetInfoDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse() {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ARTIST_GET_INFO);
    }

    /**
     * Helper method to create API response for testing with a specific entity
     */
    private LastfmApiResponse createApiResponse(LastfmArtist artist) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ARTIST_GET_INFO, artist);
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistGetInfoResponseAndNoExistingArtistProvided() throws Exception {
        // given
        ArtistGetInfoArtistDto artistDto = dtoRoot.getArtist();
        String artistName = artistDto.getName();
        int expectedTagsCount = artistDto.getTagsObject().getTags().size();
        int expectedSimilarArtistsCount = artistDto.getSimilarArtistsObject().getArtists().size();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was created
        Optional<LastfmArtist> savedArtist = artistRepository.findByName(artistName);
        assertTrue(savedArtist.isPresent(), "Artist should be saved to database");
        
        // Verify tags were created
        List<LastfmTag> savedTags = tagRepository.findAll();
        assertEquals(expectedTagsCount, savedTags.size(), "All tags should be saved to database");
        
        // Verify artist-tag relations were created
        verifyArtistTagCount(savedArtist.get(), expectedTagsCount);


        
        // Verify similar artist relations were created
        verifyArtistSimilarCount(savedArtist.get(), expectedSimilarArtistsCount);
        
        // Verify all similar artists have correct relation type
        List<LastfmArtistsRelation> artistRelations = artistsRelationRepository.findByTargetArtistId(savedArtist.get().getId());
        for (LastfmArtistsRelation relation : artistRelations) {
            assertEquals(LastfmEntityRelationType.SIMILARITY, relation.getRelationType(), 
                "Artist relation should be of type SIMILARITY");
            assertEquals(savedArtist.get().getId(), relation.getTargetArtist().getId(),
                "Target artist should be the main artist");
        }
        
        // Verify attribute history records were created
        List<LastfmAttributeHistoryRecord> attributeRecords = attributeHistoryRepository.findAll();
        assertFalse(attributeRecords.isEmpty(), "Attribute history records should be created");
        
        // Check specific artist attributes
        LastfmArtist artist = savedArtist.get();
        assertEquals(artistName, artist.getName(), "Artist name should match");
        assertEquals(artistDto.getMbid(), artist.getMbid(), "Artist MBID should match");
        assertEquals(artistDto.getUrl(), artist.getUrl(), "Artist URL should match");
        assertEquals(artistDto.getStats().getListeners(), artist.getListenersCount(), "Artist listeners count should match");
        assertEquals(artistDto.getStats().getPlayCount(), artist.getPlayCount(), "Artist play count should match");
        
        // Verify specific attribute history records using the test helper
        testHelper.verifyStringAttribute(artist, LastfmAttribute.MBID, artistDto.getMbid());
        testHelper.verifyStringAttribute(artist, LastfmAttribute.URL, artistDto.getUrl());
        testHelper.verifyIntAttribute(artist, LastfmAttribute.LISTENERS_COUNT, artistDto.getStats().getListeners());
        testHelper.verifyIntAttribute(artist, LastfmAttribute.PLAY_COUNT, artistDto.getStats().getPlayCount());
    }

    @Test
    void process_shouldUpdateExistingArtist_whenArtistGetInfoResponseAndExistingArtistProvided() throws Exception {
        // given
        ArtistGetInfoArtistDto artistDto = dtoRoot.getArtist();
        String artistName = artistDto.getName();
        
        // Create existing artist with minimal data and APPROVED status
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(artistName)
                   .url("http://old-url.com")
                   .listenersCount(100)
                   .playCount(200)
                   .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingArtist);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialArtistTagCount = artistTagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Artist should still exist in database");
        
        // Verify artist data was updated
        LastfmArtist artist = updatedArtist.get();
        assertEquals(artistName, artist.getName(), "Artist name should remain the same");
        assertNotEquals("http://old-url.com", artist.getUrl(), "Artist URL should be updated");
        assertEquals(artistDto.getUrl(), artist.getUrl(), "Artist URL should match the response");
        assertNotEquals(100, artist.getListenersCount(), "Artist listeners count should be updated");
        assertEquals(artistDto.getStats().getListeners(), artist.getListenersCount(), "Artist listeners count should match the response");
        assertNotEquals(200, artist.getPlayCount(), "Artist play count should be updated");
        assertEquals(artistDto.getStats().getPlayCount(), artist.getPlayCount(), "Artist play count should match the response");
        
        // Verify approval status is preserved
        assertEquals(ApprovalStatus.APPROVED, artist.getApprovalStatus(), "Artist approval status should be preserved");
        
        // Verify new tags were created
        int expectedTagsCount = artistDto.getTagsObject().getTags().size();
        assertTrue(tagRepository.count() > initialTagCount, "New tags should be added to database");
        
        // Verify new artist-tag relations were created
        assertTrue(artistTagRepository.count() > initialArtistTagCount, 
            "New artist-tag relations should be created");
        
        // Verify new attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws Exception {
        // given
        LastfmApiResponse apiResponse = createApiResponse();
        
        // when
        processor.processResponse(apiResponse);
        
        // Record counts after first processing
        long artistCount = artistRepository.count();
        long tagCount = tagRepository.count();
        long artistTagCount = artistTagRepository.count();
        long artistRelationCount = artistsRelationRepository.count();
        long attributeCount = attributeHistoryRepository.count();
        
        // Process again
        processor.processResponse(apiResponse);
        
        // then
        // Verify counts remain the same
        assertEquals(artistCount, artistRepository.count(), 
            "Artist count should remain the same after second processing");
        assertEquals(tagCount, tagRepository.count(), 
            "Tag count should remain the same after second processing");
        assertEquals(artistTagCount, artistTagRepository.count(), 
            "Artist-tag relation count should remain the same after second processing");
        assertEquals(artistRelationCount, artistsRelationRepository.count(), 
            "Artist-artist relation count should remain the same after second processing");
        assertEquals(attributeCount, attributeHistoryRepository.count(),
            "Attribute history record count should remain the same after second processing");
    }

    @Test
    void process_shouldHandleEmptyTags_whenResponseHasNoTags() throws Exception {
        // given
        // Create a modified response with empty tags
        ObjectMapper objectMapper = new ObjectMapper();
        ArtistGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, ArtistGetInfoDtoRoot.class);
        modifiedDtoRoot.getArtist().getTagsObject().setTags(List.of()); // Set empty tags list
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);
        
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ARTIST_GET_INFO);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was created
        Optional<LastfmArtist> savedArtist = artistRepository.findByName(dtoRoot.getArtist().getName());
        assertTrue(savedArtist.isPresent(), "Artist should be saved to database");
        
        // Verify no tags were created
        verifyArtistTagCount(savedArtist.get(), 0);
    }

    @Test
    void process_shouldHandleEmptySimilarArtists_whenResponseHasNoSimilarArtists() throws Exception {
        // given
        // Create a modified response with empty similar artists
        ObjectMapper objectMapper = new ObjectMapper();
        ArtistGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, ArtistGetInfoDtoRoot.class);
        modifiedDtoRoot.getArtist().getSimilarArtistsObject().setArtists(List.of()); // Set empty similar artists list
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);
        
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ARTIST_GET_INFO);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was created
        Optional<LastfmArtist> savedArtist = artistRepository.findByName(dtoRoot.getArtist().getName());
        assertTrue(savedArtist.isPresent(), "Artist should be saved to database");
        
        // Verify no artist relations were created
        verifyArtistSimilarCount(savedArtist.get(), 0);
    }

    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        String invalidJson = "{\"artist\": {\"invalid\": true}}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.ARTIST_GET_INFO);
        
        // when/then
        assertThrows(RuntimeException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(0, artistRepository.count(), "No artists should be created");
        assertEquals(0, tagRepository.count(), "No tags should be created");
        assertEquals(0, artistTagRepository.count(), "No artist-tag relations should be created");
        assertEquals(0, artistsRelationRepository.count(), "No artist-artist relations should be created");
    }

    @Test
    void process_shouldVerifyTagRelationAttributes_whenProcessingResponse() throws Exception {
        // given
        LastfmApiResponse apiResponse = createApiResponse();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was created
        Optional<LastfmArtist> savedArtist = artistRepository.findByName(dtoRoot.getArtist().getName());
        assertTrue(savedArtist.isPresent(), "Artist should be saved to database");
        
        // Get all artist-tag relations
        List<LastfmArtistTag> artistTagRelations = artistTagRepository.findByArtistId(savedArtist.get().getId());
        assertFalse(artistTagRelations.isEmpty(), "Artist-tag relations should be created");
        
        // Verify each relation has the correct artist and API call
        for (LastfmArtistTag relation : artistTagRelations) {
            assertEquals(savedArtist.get().getId(), relation.getArtist().getId(),
                "Relation should reference the correct artist");
            assertEquals(apiResponse.getApiCall().getId(), relation.getApiCall().getId(),
                "Relation should reference the correct API call");
            assertNotNull(relation.getTag(), "Relation should have a tag");
        }
        
        // Verify tag names match those in the response
        List<String> expectedTagNames = dtoRoot.getArtist().getTagsObject().getTags().stream()
            .map(tag -> tag.getName().toLowerCase())
            .toList();
        
        List<String> actualTagNames = artistTagRelations.stream()
            .map(relation -> relation.getTag().getName().toLowerCase())
            .toList();
        
        assertTrue(actualTagNames.containsAll(expectedTagNames),
            "All expected tags should be present in the relations");
    }


    /**
     * Verify that artist has the expected number of tags
     *
     * @param artist The artist to check
     * @param expectedCount The expected number of tags
     */
    public void verifyArtistTagCount(LastfmArtist artist, int expectedCount) {
        List<LastfmArtistTag> artistTags = artistTagRepository.findByArtistId(artist.getId());
        assertEquals(expectedCount, artistTags.size(),
            "Artist should have " + expectedCount + " tags");
    }

    /**
     * Verify that artist has the expected number of similar artists
     *
     * @param artist The artist to check
     * @param expectedCount The expected number of similar artists
     */
    public void verifyArtistSimilarCount(LastfmArtist artist, int expectedCount) {
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findByTargetArtistId(artist.getId());
        assertEquals(expectedCount, relations.size(),
            "Artist should have " + expectedCount + " similar artists");
    }

}
