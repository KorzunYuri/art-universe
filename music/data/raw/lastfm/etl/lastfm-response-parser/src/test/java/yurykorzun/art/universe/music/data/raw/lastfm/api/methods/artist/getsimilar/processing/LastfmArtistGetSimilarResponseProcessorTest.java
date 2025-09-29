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
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.TestLastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.TestLastfmArtistsRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.BaseLastfmApiResponseProcessorTest;

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
    // entities
    LastfmArtistServiceImpl.class,
    // relations
    LastfmArtistsRelationServiceImpl.class,
})
class LastfmArtistGetSimilarResponseProcessorTest extends BaseLastfmApiResponseProcessorTest {
    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistGetSimilarResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private TestLastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private TestLastfmArtistsRelationRepository artistsRelationRepository;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

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

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify new artists were created
        int expectedNewArtistsCount = dtoRoot.getRootObject().getArtists().size();
        assertEquals(initialArtistCount + expectedNewArtistsCount, artistRepository.count(),
            "New artists should be created");

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

    @Test
    void process_shouldExcludeSimilarArtist_whenMbidEqualsToSourceArtistMbid() throws Exception {
        // given
        // Create source artist with same MBID as one in the test response
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Cher")
                .mbid("c43d2302-02db-487b-b62d-8cb3c57f94c6")
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialRelationCount = artistsRelationRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify that one less artist was created (source artist excluded)
        int totalArtistsInResponse = dtoRoot.getRootObject().getArtists().size();
        int expectedNewArtistsCount = totalArtistsInResponse - 1; // Exclude source artist
        assertEquals(initialArtistCount + expectedNewArtistsCount, artistRepository.count(),
            "Source artist should be excluded from similar artists");

        // Verify that relations were created only for non-source artists
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findByTargetArtistId(sourceArtist.getId());
        assertEquals(expectedNewArtistsCount, relations.size(),
            "Relations should be created only for non-source artists");

        // Verify that none of the created relations point to the source artist as source
        for (LastfmArtistsRelation relation : relations) {
            assertNotEquals(sourceArtist.getId(), relation.getSourceArtist().getId(),
                "Source artist should not appear as source in any relation");
            assertNotEquals(sourceArtist.getMbid(), relation.getSourceArtist().getMbid(),
                "Source artist MBID should not appear in any similar artist");
        }
    }

    @Test
    void process_shouldExcludeSourceArtistByName_whenSourceArtistHasNoMbidButNameMatches() throws Exception {
        // given
        // Create source artist with same name but no MBID
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Sonny & Cher")
                .mbid(null) // No MBID
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);

        // Record initial state
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify that one less artist was created (source artist excluded by name)
        int totalArtistsInResponse = dtoRoot.getRootObject().getArtists().size();
        int expectedNewArtistsCount = totalArtistsInResponse - 1; // Exclude source artist
        assertEquals(initialArtistCount + expectedNewArtistsCount, artistRepository.count(),
            "Source artist should be excluded from similar artists by name comparison");

        // Verify that relations were created only for non-source artists
        List<LastfmArtistsRelation> relations = artistsRelationRepository.findByTargetArtistId(sourceArtist.getId());
        assertEquals(expectedNewArtistsCount, relations.size(),
            "Relations should be created only for non-source artists");

        // Verify that none of the created relations have the same name as source artist
        for (LastfmArtistsRelation relation : relations) {
            assertNotEquals(sourceArtist.getName().toLowerCase(),
                relation.getSourceArtist().getName().toLowerCase(),
                "Source artist name should not appear in any similar artist");
        }
    }

    @Test
    void process_shouldSkipBlacklistedSimilarArtists_whenSomeArtistsAreBlacklisted() throws Exception {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getRootObject().getArtists().get(0).getName()) // Use first artist as source
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist some similar artists from the response
        var similarArtists = dtoRoot.getRootObject().getArtists();
        if (similarArtists.size() > 2) {
            // Blacklist the second artist (first is source, so skip it)
            blacklistService.addToBlacklist(LastfmEntityType.ARTIST,
                similarArtists.get(1).getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);

        // Record initial state
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify some but not all similar artists were created
        long finalArtistCount = artistRepository.count();
        assertTrue(finalArtistCount > initialArtistCount, "Some similar artists should be created");

        // Should be less than total because one is blacklisted and one is the source artist
        int expectedMaxArtists = similarArtists.size() - 1; // Exclude source artist
        assertTrue(finalArtistCount <= initialArtistCount + expectedMaxArtists,
            "Not all similar artists should be created due to blacklist and source exclusion");

        // Verify relationships were created
        assertTrue(artistsRelationRepository.count() > 0, "Some artist-artist relationships should be created");
    }

    @Test
    void process_shouldHandleAllSimilarArtistsBlacklisted_whenAllNonSourceArtistsAreBlacklisted() throws Exception {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getRootObject().getArtists().get(0).getName()) // Use first artist as source
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist ALL similar artists except the source (skip index 0)
        var similarArtists = dtoRoot.getRootObject().getArtists();
        for (int i = 1; i < similarArtists.size(); i++) {
            blacklistService.addToBlacklist(LastfmEntityType.ARTIST,
                similarArtists.get(i).getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify only source artist exists (no new similar artists created)
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, artistsRelationRepository.count(), "No artist-artist relationships should be created");
    }

    @Test
    void process_shouldProcessNormally_whenNoArtistsAreBlacklisted() throws Exception {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getRootObject().getArtists().get(0).getName()) // Use first artist as source
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_SIMILAR, sourceArtist);

        // Record initial state
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify similar artists were created (excluding source and those below threshold)
        long finalArtistCount = artistRepository.count();
        assertTrue(finalArtistCount > initialArtistCount, "Similar artists should be created");

        // Verify relationships were created
        assertTrue(artistsRelationRepository.count() > 0, "Artist-artist relationships should be created");
    }
}