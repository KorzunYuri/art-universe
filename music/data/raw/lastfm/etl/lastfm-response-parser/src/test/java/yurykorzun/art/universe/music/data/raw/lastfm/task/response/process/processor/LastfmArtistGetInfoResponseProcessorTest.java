package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmParserProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.EntityTextContent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.getinfo.LastfmArtistGetInfoArtistFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.getinfo.LastfmArtistGetInfoSimilarArtistFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.getinfo.LastfmArtistGetInfoTagFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.BaseLastfmApiResponseProcessorTest;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.TestEntityTextContentRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.TestLastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmArtistsRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import({
    // processing
    LastfmArtistGetInfoResponseProcessor.class,
    LastfmArtistGetInfoArtistFactory.class,
    LastfmArtistGetInfoSimilarArtistFactory.class,
    LastfmArtistGetInfoTagFactory.class,
    // entities
    LastfmArtistServiceImpl.class,
    LastfmTagServiceImpl.class,
    // relations
    LastfmArtistsRelationServiceImpl.class,
    LastfmArtistTagServiceImpl.class,
})
class LastfmArtistGetInfoResponseProcessorTest extends BaseLastfmApiResponseProcessorTest {

    @Autowired
    private LastfmArtistGetInfoResponseProcessor processor;

    @Autowired
    private TestLastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private TestLastfmArtistTagRepository artistTagRepository;

    @Autowired
    private TestLastfmArtistsRelationRepository artistsRelationRepository;

    @Autowired
    private TestEntityTextContentRepository textContentRepository;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    private static final String TEST_RESPONSE_KEY = "artist.getInfo";
    private String responseJsonString;
    private ArtistGetInfoDtoRoot dtoRoot;

    @BeforeEach
    public void setUp() throws IOException {

        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
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

        // Check specific artist attributes
        LastfmArtist artist = savedArtist.get();
        assertEquals(artistName, artist.getName(), "Artist name should match");
        assertEquals(artistDto.getMbid(), artist.getMbid(), "Artist MBID should match");
        assertEquals(artistDto.getUrl(), artist.getUrl(), "Artist URL should match");
        assertEquals(artistDto.getStats().getListeners(), artist.getListenersCount(), "Artist listeners count should match");
        assertEquals(artistDto.getStats().getPlayCount(), artist.getPlayCount(), "Artist play count should match");

        // Verify bio text content was saved
        List<EntityTextContent> textContents = textContentRepository.findByEntityTypeAndEntityId(
                LastfmEntityType.ARTIST, artist.getId());
        assertEquals(2, textContents.size(), "Both bio summary and bio content should be saved");
        assertTrue(textContents.stream().anyMatch(tc -> tc.getContentType() == TextContentType.BIO_SUMMARY),
                "Bio summary should be saved");
        assertTrue(textContents.stream().anyMatch(tc -> tc.getContentType() == TextContentType.BIO_CONTENT),
                "Bio content should be saved");
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
                   .mbid("mbid")
                   .listenersCount(100)
                   .playCount(200L)
                   .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingArtist);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialArtistTagCount = artistTagRepository.count();

        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Artist should still exist in database");
        
        // Verify artist data was updated
        LastfmArtist artist = updatedArtist.get();
        assertEquals(artistName, artist.getName(), "Artist name should remain the same");
        assertNotEquals(100, artist.getListenersCount(), "Artist listeners count should be updated");
        assertEquals(artistDto.getStats().getListeners(), artist.getListenersCount(), "Artist listeners count should match the response");
        assertNotEquals(200, artist.getPlayCount(), "Artist play count should be updated");
        assertEquals(artistDto.getStats().getPlayCount(), artist.getPlayCount(), "Artist play count should match the response");
        // constant attributes
        assertEquals("http://old-url.com", artist.getUrl(), "Existing URL should not be updated");
        assertEquals("mbid", artist.getMbid(), "Existing MBID should not be updated");

        // Verify approval status is preserved
        assertEquals(ApprovalStatus.APPROVED, artist.getApprovalStatus(), "Artist approval status should be preserved");
        
        // Verify new tags were created
        int expectedTagsCount = artistDto.getTagsObject().getTags().size();
        assertTrue(tagRepository.count() > initialTagCount, "New tags should be added to database");
        
        // Verify new artist-tag relations were created
        assertTrue(artistTagRepository.count() > initialArtistTagCount, 
            "New artist-tag relations should be created");
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

    @Test
    void process_shouldSkipProcessing_whenMainArtistFailsValidation() throws Exception {
        // given
        // Create a modified response with low listeners count for main artist
        ObjectMapper objectMapper = new ObjectMapper();
        ArtistGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, ArtistGetInfoDtoRoot.class);
        modifiedDtoRoot.getArtist().getStats().setListeners(500); // Below threshold of 1000
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);

        when(configPropertyHolder.getInt(LastfmParserProperty.THRESHOLD_ARTIST_LISTENERS_COUNT)).thenReturn(1000);

        // Create existing artist
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getArtist().getName())
                .url(dtoRoot.getArtist().getUrl())
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ARTIST_GET_INFO, existingArtist);

        // when/then
        assertDoesNotThrow(() -> processor.processResponse(apiResponse), "Should not throw exception when main artist fails validation");

        // Verify no entities were processed
        assertEquals(1, artistRepository.count(), "Only the existing artist should remain");
        assertEquals(0, tagRepository.count(), "No tags should be created");
        assertEquals(0, artistTagRepository.count(), "No artist-tag relationships should be created");
        assertEquals(0, artistsRelationRepository.count(), "No artist-artist relationships should be created");
    }

    @Test
    void process_shouldSkipBlacklistedSimilarArtists_whenSomeArtistsAreBlacklisted() throws Exception {
        // given
        // Create existing artist
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getArtist().getName())
                .url(dtoRoot.getArtist().getUrl())
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist some similar artists from the response
        var similarArtists = dtoRoot.getArtist().getSimilarArtistsObject().getArtists();
        if (similarArtists.size() > 1) {
            // Blacklist the first similar artist
            blacklistService.addToBlacklist(LastfmEntityType.ARTIST,
                similarArtists.get(0).getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_INFO, existingArtist);

        // Record initial state
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify main artist was updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Main artist should still exist");

        // Verify some but not all similar artists were created
        long finalArtistCount = artistRepository.count();
        assertTrue(finalArtistCount > initialArtistCount, "Some similar artists should be created");
        assertTrue(finalArtistCount < initialArtistCount + similarArtists.size(), 
            "Not all similar artists should be created due to blacklist");

        // Verify tags were still processed
        assertTrue(tagRepository.count() > 0, "Tags should be processed");
        assertTrue(artistTagRepository.count() > 0, "Artist-tag relationships should be created");
    }

    @Test
    void process_shouldSkipBlacklistedTags_whenSomeTagsAreBlacklisted() throws Exception {
        // given
        // Create existing artist
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getArtist().getName())
                .url(dtoRoot.getArtist().getUrl())
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist some tags from the response
        var tags = dtoRoot.getArtist().getTagsObject().getTags();
        if (tags.size() > 1) {
            // Blacklist the first tag
            blacklistService.addToBlacklist(LastfmEntityType.TAG,
                tags.get(0).getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_INFO, existingArtist);

        // Record initial state
        long initialTagCount = tagRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify main artist was updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Main artist should still exist");

        // Verify some but not all tags were created
        long finalTagCount = tagRepository.count();
        assertTrue(finalTagCount > initialTagCount, "Some tags should be created");
        assertTrue(finalTagCount < initialTagCount + tags.size(), 
            "Not all tags should be created due to blacklist");

        // Verify similar artists were still processed
        assertTrue(artistRepository.count() > 1, "Similar artists should be processed");
        assertTrue(artistsRelationRepository.count() > 0, "Artist-artist relationships should be created");
    }

    @Test
    void process_shouldHandleAllSimilarArtistsBlacklisted_whenAllArtistsAreBlacklisted() throws Exception {
        // given
        // Create existing artist
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getArtist().getName())
                .url(dtoRoot.getArtist().getUrl())
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist ALL similar artists from the response
        dtoRoot.getArtist().getSimilarArtistsObject().getArtists().stream()
            .map(artist -> artist.getUrl())
            .forEach(artistUrl -> blacklistService.addToBlacklist(
                LastfmEntityType.ARTIST, artistUrl));

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_INFO, existingArtist);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify main artist was still updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Main artist should still exist");

        // Verify no similar artists were created
        assertEquals(1, artistRepository.count(), "Only main artist should exist");
        assertEquals(0, artistsRelationRepository.count(), "No artist-artist relationships should be created");

        // Verify tags were still processed
        assertTrue(tagRepository.count() > 0, "Tags should still be processed");
        assertTrue(artistTagRepository.count() > 0, "Artist-tag relationships should still be created");
    }

    @Test
    void process_shouldHandleAllTagsBlacklisted_whenAllTagsAreBlacklisted() throws Exception {
        // given
        // Create existing artist
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getArtist().getName())
                .url(dtoRoot.getArtist().getUrl())
                .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist ALL tags from the response
        dtoRoot.getArtist().getTagsObject().getTags().stream()
            .map(tag -> tag.getUrl())
            .forEach(tagUrl -> blacklistService.addToBlacklist(
                LastfmEntityType.TAG, tagUrl));

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_INFO, existingArtist);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify main artist was still updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Main artist should still exist");

        // Verify no tags were created
        assertEquals(0, tagRepository.count(), "No tags should be created");
        assertEquals(0, artistTagRepository.count(), "No artist-tag relationships should be created");

        // Verify similar artists were still processed
        assertTrue(artistRepository.count() > 1, "Similar artists should still be processed");
        assertTrue(artistsRelationRepository.count() > 0, "Artist-artist relationships should still be created");
    }

    @Test
    void process_shouldNotSaveTextContent_whenBioIsNull() throws Exception {
        // given - create a response JSON without bio field
        String noBioJson = responseJsonString.replaceAll(",\\s*\"bio\"\\s*:\\s*\\{[^}]*\"links\"[^}]*\\}[^}]*\\}", "");
        ArtistGetInfoDtoRoot noBioRoot = parseResponse(noBioJson);
        assertNull(noBioRoot.getArtist().getBio(), "Bio should be null in modified response");

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
                noBioJson, LastfmApiCallType.ARTIST_GET_INFO);

        // when
        processor.processResponse(apiResponse);

        // then
        List<EntityTextContent> textContents = textContentRepository.findAll();
        assertEquals(0, textContents.size(), "No text content should be saved when bio is null");
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
