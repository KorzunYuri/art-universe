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
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    LastfmArtistSearchResponseProcessor.class,
    LastfmArtistSearchArtistFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmArtistServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
})
class LastfmArtistSearchResponseProcessorTest extends JpaOnlyTest {

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

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewEntities_whenArtistSearchApiResponseAndEmptyDatabaseProvided() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.search");
        String searchString = "PUP";
        
        // Create API call with search parameter
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, searchString))
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(responseBody, sourceApiCall);

        entityManager.flush();
        
        // Set threshold to 0 to process all artists
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 0.0);
        
        // Parse response to get expected count
        ArtistSearchDtoRoot dtoRoot = parseResponse(responseBody);
        int expectedArtistsCount = dtoRoot.getRootObject().getMatches().getArtists().size();
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new artists were created
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artists have expected attributes
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            // URL is one of the attributes that should be set
            List<LastfmAttributeHistoryRecord> artistAttributes = attributeHistoryRepository.findAll().stream()
                .filter(attr -> attr.getEntityId() == artist.getId())
                .toList();
            assertFalse(artistAttributes.isEmpty(), "Artist should have attributes");
        }
    }

    @Test
    void process_shouldFilterArtistsByThreshold_whenThresholdIsSet() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.search");
        String searchString = "PUP";
        
        // Create API call with search parameter
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, searchString))
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(responseBody, sourceApiCall);
        
        // Set high threshold to filter most artists
        double threshold = 0.8;
        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", threshold);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();

        entityManager.flush();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only a few artists were created (those with high similarity)
        long newArtistsCount = artistRepository.count() - initialArtistCount;
        assertTrue(newArtistsCount > 0, "Some artists should be created");
        
        // Parse response to get total count
        ArtistSearchDtoRoot dtoRoot = parseResponse(responseBody);
        int totalArtistsInResponse = dtoRoot.getRootObject().getMatches().getArtists().size();
        
        // Verify filtering worked
        assertTrue(newArtistsCount < totalArtistsInResponse, 
            "Number of created artists should be less than total artists in response due to filtering");
    }

    @Test
    void process_shouldCreateNoEntities_whenNoArtistsPassThreshold() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.search");
        String searchString = "PUP";
        
        // Create API call with search parameter
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, searchString))
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(responseBody, sourceApiCall);
        
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

    private ArtistSearchDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistSearchDtoRoot.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
