package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.utils.StringUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TestTaskCoordinatorConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
        // processing
        LastfmArtistSearchResponseProcessor.class,
        LastfmArtistSearchArtistFactory.class,
        LastfmApiDtoProcessingService.class,
        // entities
        LastfmArtistServiceImpl.class,
        // attributes
        LastfmAttributeHistoryServiceImpl.class,
        LastfmAttributeTypeSynchronizer.class,
        LastfmAttributeHistoryProcessor.class,
        TestTaskCoordinatorConfig.class,
})
class LastfmArtistSearchResponseProcessorTest extends JpaTestWithHelper {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistSearchResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private EntityManager entityManager;
    
    private static final String TEST_RESPONSE_KEY = "artist.search";
    private static final String SEARCH_STRING = "PUP";
    private String responseJsonString;
    private ArtistSearchDtoRoot dtoRoot;

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
    private ArtistSearchDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistSearchDtoRoot.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse() {
        // Create API call with search parameter
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, SEARCH_STRING))
        );
        
        // Create API response
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, sourceApiCall);
    }

    @Test
    void process_shouldCreateNewEntities_whenArtistSearchApiResponseProvided() throws IOException {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Set threshold to 0 to process all artists
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 0.0);
        
        // Calculate expected counts
        int expectedArtistsCount = dtoRoot.getRootObject().getMatches().getArtists().size();
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // Calculate expected attribute count
        // Each artist has 1 attributes (LISTENERS_COUNT) from artistAttrHandlers. URL & MBID have been excluded from history
        int expectedAttributesPerArtist = 1;
        int expectedTotalAttributeCount = expectedArtistsCount * expectedAttributesPerArtist;
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new artists were created
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Verify artists have expected attributes
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            
            // Find corresponding artist in the DTO
            var artistDto = dtoRoot.getRootObject().getMatches().getArtists().stream()
                .filter(dto -> dto.getName().equals(artist.getName()))
                .findFirst();
            
            if (artistDto.isPresent()) {
                // Verify attributes if this is one of the artists from the response
                if (artistDto.get().getMbid() != null && !artistDto.get().getMbid().isEmpty()) {
                    assertEquals(artistDto.get().getMbid(), artist.getMbid(), "Artist MBID should match");
                }
                
                assertEquals(artistDto.get().getUrl(), artist.getUrl(), "Artist URL should match");

                assertEquals(artistDto.get().getListenersCount(), artist.getListenersCount(),
                    "Artist listeners count should match");
            }
        }
    }

    @Test
    void process_shouldFilterArtistsByThreshold_whenThresholdIsSet() throws IOException {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Set high threshold to filter most artists
        double threshold = 0.8;
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", threshold);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // Calculate expected counts
        long expectedArtistsCount = dtoRoot.getRootObject().getMatches().getArtists().stream()
            .filter(artist -> StringUtils.getSimilarity(artist.getName(), SEARCH_STRING) > threshold)
            .count();
        
        // Calculate expected attribute count
        int expectedAttributesPerArtist = 1; // LISTENERS_COUNT. MBID & URL have been excluded
        long expectedTotalAttributeCount = expectedArtistsCount * expectedAttributesPerArtist;
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only artists above threshold were created
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "Only artists above threshold should be created");

        // Verify filtering worked
        int totalArtistsInResponse = dtoRoot.getRootObject().getMatches().getArtists().size();
        assertTrue(expectedArtistsCount < totalArtistsInResponse, 
            "Number of created artists should be less than total artists in response due to filtering");
    }

    @Test
    void process_shouldCreateNoEntities_whenNoArtistsPassThreshold() throws IOException {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Set extremely high threshold so no artists pass
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 1.0);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify no new artists were created
        assertEquals(initialArtistCount, artistRepository.count(), 
            "No new artists should be created when threshold is too high");
        
        // Verify no new attribute records were created
        assertEquals(initialAttributeCount, attributeHistoryRepository.count(), 
            "No new attribute records should be created when threshold is too high");
    }
    
    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws Exception {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Set threshold to 0 to process all artists
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 0.0);
        
        // when
        processor.processResponse(apiResponse);
        
        // Record counts after first processing
        long artistCount = artistRepository.count();
        long attributeCount = attributeHistoryRepository.count();
        
        // Process again
        processor.processResponse(apiResponse);
        
        // then
        // Verify counts remain the same
        assertEquals(artistCount, artistRepository.count(), 
            "Artist count should remain the same after second processing");
        assertEquals(attributeCount, attributeHistoryRepository.count(),
            "Attribute history record count should remain the same after second processing");
    }
    
    @Test
    void process_shouldPreserveApprovalStatus_whenUpdatingExistingArtists() throws Exception {
        // given
        // Create one artist with APPROVED status that matches one in the response
        LastfmArtist approvedArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("PUP") // This name exists in the test response
                   .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // Set threshold to 0 to process all artists
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 0.0);
        
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
        // Create API call with search parameter
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, SEARCH_STRING))
        );
        
        // Create invalid API response
        String invalidJson = "{\"results\": {\"invalid\": true}}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, sourceApiCall);
        
        // when/then
        assertThrows(RuntimeException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(0, artistRepository.count(), "No artists should be created");
    }
    
    @Test
    void process_shouldHandleEmptyArtistMatches_whenResponseHasNoArtists() throws Exception {
        // given
        // Create a modified response with empty artists list
        ObjectMapper objectMapper = new ObjectMapper();
        ArtistSearchDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, ArtistSearchDtoRoot.class);
        modifiedDtoRoot.getRootObject().getMatches().setArtists(List.of()); // Set empty artists list
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);
        
        // Create API call with search parameter
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, SEARCH_STRING))
        );
        
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, sourceApiCall);
        
        // Set threshold to 0 to process all artists
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 0.0);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify no new artists or attributes were created
        assertEquals(0, artistRepository.count(), "No artists should be created");
    }
}
