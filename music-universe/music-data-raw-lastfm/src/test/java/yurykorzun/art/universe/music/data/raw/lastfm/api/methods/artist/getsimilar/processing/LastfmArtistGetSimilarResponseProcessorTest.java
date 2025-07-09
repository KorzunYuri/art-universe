package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.testing.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistsRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmArtistGetSimilarResponseProcessor.class,
    LastfmArtistGetSimilarArtistFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmArtistServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmArtistsRelationServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmArtistGetSimilarResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistGetSimilarResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistsRelationRepository artistsRelationRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "artist.getSimilar";
    private String responseJsonString;
    private ArtistGetSimilarDtoRoot dtoRoot;
    private static final float DEFAULT_THRESHOLD = 0.0f;

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
        
        // Set threshold to 0 to process all similar artists by default
        ReflectionTestUtils.setField(processor, "artistMatchThreshold", DEFAULT_THRESHOLD);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private ArtistGetSimilarDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistGetSimilarDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(LastfmArtist artist) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ARTIST_GET_SIMILAR, artist);
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistGetSimilarResponseProvided() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialRelationCount = artistsRelationRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new artists were created
        int expectedNewArtistsCount = dtoRoot.getRootObject().getArtists().size();
        assertEquals(initialArtistCount + expectedNewArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Calculate expected attribute counts
        // Each similar artist has 2 attributes (MBID, URL) from artistAttrHandlers
        int expectedNonScopedAttributeCount = expectedNewArtistsCount * 2;
        // Each relation has 1 scoped attribute (MATCH_COEFF)
        int expectedScopedAttributeCount = expectedNewArtistsCount;
        int expectedTotalAttributeCount = expectedNonScopedAttributeCount + expectedScopedAttributeCount;
        
        // Verify attribute history records were created
        assertEquals(initialAttributeCount + expectedTotalAttributeCount, attributeHistoryRepository.count(), 
            "Expected number of attribute history records should be created");
        
        // Verify artist-artist relations were created
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findByTargetArtistId(sourceArtist.getId());
        assertEquals(expectedNewArtistsCount, relations.size(), 
            "Artist-artist relations should be created");
        
        // Verify relation properties
        for (LastfmArtistsRelation relation : relations) {
            assertEquals(LastfmEntityRelationType.SIMILARITY, relation.getRelationType(), 
                "Relation type should be SIMILARITY");
            assertEquals(sourceArtist.getId(), relation.getTargetArtist().getId(), 
                "Target artist should be the source artist");
            assertNotNull(relation.getMatchScore(), "Match score should be set");
            
            // Find corresponding artist in the DTO to get the original match coefficient
            var similarArtistDto = dtoRoot.getRootObject().getArtists().stream()
                .filter(dto -> dto.getName().equals(relation.getSourceArtist().getName()))
                .findFirst()
                .orElseThrow();
            
            // Calculate expected match coefficient value exactly as in the processor
            int expectedMatchCoeff = BigDecimal.valueOf(similarArtistDto.getMatchCoeff())
                .multiply(new BigDecimal(100))
                .intValue();
            
            // Verify match score attribute history record exists using the scoped attribute method
            testHelper.verifyIntAttributeWithScope(
                relation.getTargetArtist(),                // entity (target artist)
                relation.getSourceArtist(),  // scope entity (similar artist)
                LastfmAttribute.MATCH_COEFF, 
                expectedMatchCoeff
            );
        }
        
        // Verify specific attributes for similar artists
        for (LastfmArtistsRelation relation : relations) {
            LastfmArtist similarArtist = relation.getSourceArtist();
            
            // Find corresponding artist in the DTO
            var similarArtistDto = dtoRoot.getRootObject().getArtists().stream()
                .filter(dto -> dto.getName().equals(similarArtist.getName()))
                .findFirst()
                .orElseThrow();
            
            // Verify attributes
            assertEquals(similarArtistDto.getMbid(), similarArtist.getMbid(), "MBID should match");
            assertEquals(similarArtistDto.getUrl(), similarArtist.getUrl(), "URL should match");
            
            // Verify attribute history records
            testHelper.verifyStringAttribute(similarArtist, LastfmAttribute.MBID, similarArtistDto.getMbid());
            testHelper.verifyStringAttribute(similarArtist, LastfmAttribute.URL, similarArtistDto.getUrl());
        }
    }

    @Test
    void process_shouldFilterArtistsByThreshold_whenThresholdIsSet() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // Set threshold to filter out some artists
        float threshold = 0.5f;
        ReflectionTestUtils.setField(processor, "artistMatchThreshold", threshold);
        
        // Count how many artists should pass the threshold
        long expectedArtistsCount = dtoRoot.getRootObject().getArtists().stream()
            .filter(artist -> artist.getMatchCoeff() > threshold)
            .count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only artists above threshold were processed
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findByTargetArtistId(sourceArtist.getId());
        assertEquals(expectedArtistsCount, relations.size(), 
            "Only artists above threshold should be processed");
        
        // Verify all relations have match score above threshold
        for (LastfmArtistsRelation relation : relations) {
            assertTrue(relation.getMatchScore().compareTo(BigDecimal.valueOf(threshold)) > 0, 
                "Match score should be above threshold");
        }
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
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws Exception {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // when
        processor.processResponse(apiResponse);
        
        // Record counts after first processing
        long artistCount = artistRepository.count();
        long relationCount = artistsRelationRepository.count();
        long attributeCount = attributeHistoryRepository.count();
        
        // Process again
        processor.processResponse(apiResponse);
        
        // then
        // Verify counts remain the same
        assertEquals(artistCount, artistRepository.count(), 
            "Artist count should remain the same after second processing");
        assertEquals(relationCount, artistsRelationRepository.count(), 
            "Artist-artist relation count should remain the same after second processing");
        assertEquals(attributeCount, attributeHistoryRepository.count(),
            "Attribute history record count should remain the same after second processing");
    }
    
    @Test
    void process_shouldPreserveApprovalStatus_whenUpdatingExistingArtists() throws Exception {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create one similar artist with APPROVED status
        LastfmArtist approvedArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Sonny & Cher") // This name exists in the test response
                   .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify the artist was updated but approval status preserved
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(approvedArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Artist should still exist in database");
        assertEquals(ApprovalStatus.APPROVED, updatedArtist.get().getApprovalStatus(), 
            "Approval status should be preserved");
    }
    
    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create invalid API response
        String invalidJson = "{\"similarartists\": {\"invalid\": true}}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);
        
        // when/then
        assertThrows(RuntimeException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, artistsRelationRepository.count(), "No artist-artist relations should be created");
    }
    
    @Test
    void process_shouldHandleEmptySimilarArtists_whenResponseHasNoArtists() throws Exception {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create a modified response with empty artists list
        ObjectMapper objectMapper = new ObjectMapper();
        ArtistGetSimilarDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, ArtistGetSimilarDtoRoot.class);
        modifiedDtoRoot.getRootObject().setArtists(List.of()); // Set empty artists list
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);
        
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify no new artists or relations were created
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, artistsRelationRepository.count(), "No artist-artist relations should be created");
    }
}
