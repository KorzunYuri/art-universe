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
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    LastfmArtistGetSimilarResponseProcessor.class,
    LastfmArtistGetSimilarArtistFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmArtistServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmArtistsRelationServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
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

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistGetSimilarResponseProvided() throws IOException {
        // given
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getSimilar");
        ArtistGetSimilarDtoRoot dtoRoot = parseResponse(responseJsonString);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);
        
        // Set threshold to 0 to process all similar artists
        ReflectionTestUtils.setField(processor, "artistMatchThreshold", 0.0f);
        
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
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-artist relations were created
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findAll();
        assertEquals(expectedNewArtistsCount, relations.size() - initialRelationCount, 
            "Artist-artist relations should be created");
        
        // Verify relation properties
        for (LastfmArtistsRelation relation : relations) {
            assertEquals(LastfmEntityRelationType.SIMILARITY, relation.getRelationType(), 
                "Relation type should be SIMILARITY");
            assertEquals(sourceArtist.getId(), relation.getTargetArtist().getId(), 
                "Target artist should be the source artist");
            assertNotNull(relation.getMatchScore(), "Match score should be set");
        }
    }

    @Test
    void process_shouldFilterArtistsByThreshold_whenThresholdIsSet() throws IOException {
        // given
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getSimilar");
        ArtistGetSimilarDtoRoot dtoRoot = parseResponse(responseJsonString);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);
        
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
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findAll();
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
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getSimilar");

        // Create source artist first (needed for API call creation)
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();

        // Create API response with the artist
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);

        // Now delete the artist to simulate non-existent artist
        artistRepository.delete(sourceArtist);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source artist not found");
    }

    private ArtistGetSimilarDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistGetSimilarDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
