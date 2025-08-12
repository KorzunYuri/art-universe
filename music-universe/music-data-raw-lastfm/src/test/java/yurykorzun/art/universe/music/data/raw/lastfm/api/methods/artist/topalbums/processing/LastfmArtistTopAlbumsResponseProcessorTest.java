package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.config.LastfmThresholdConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl.LastfmAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.blacklist.service.BlacklistedEntityUrlService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmArtistTopAlbumsResponseProcessor.class,
    LastfmApiDtoProcessingService.class,
    // quality control
    BlacklistedEntityUrlService.class,
    DtoQualityService.class,
    LastfmThresholdConfig.class,
    // entities
    LastfmAlbumServiceImpl.class,
    LastfmArtistServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmArtistAlbumServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmArtistTopAlbumsResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopAlbumsResponseProcessor processor;

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    @Autowired
    private LastfmArtistAlbumRepository artistAlbumRepository;

    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "artist.getTopAlbums";
    private String responseJsonString;
    private ArtistTopAlbumsDtoRoot dtoRoot;
    private static final int DEFAULT_THRESHOLD = 0;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Load test data once for all tests
        responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse(TEST_RESPONSE_KEY);
        dtoRoot = parseResponse(responseJsonString);
        
        // Set threshold to 0 to process all albums by default
        ReflectionTestUtils.setField(processor, "albumPlayCountThreshold", DEFAULT_THRESHOLD);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private ArtistTopAlbumsDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, ArtistTopAlbumsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(LastfmArtist artist) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ARTIST_TOP_ALBUMS, artist);
    }

    @Test
    void process_shouldCreateNewRecordsWithCorrectAttributes_whenArtistTopAlbumsResponseProvided() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getTopAlbumsObject().getAlbums().get(0).getArtist().getName())
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);

        // Record initial state
        long initialAlbumCount = albumRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistAlbumCount = artistAlbumRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify new albums were created
        int expectedAlbumsCount = 20; // after Blacklisting, was dtoRoot.getTopAlbumsObject().getAlbums().size(); before
        assertEquals(initialAlbumCount + expectedAlbumsCount, albumRepository.count(),
            "New albums should be created");

        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount,
            "New attribute history records should be created");

        // Verify artist-album relations were created
        assertEquals(initialArtistAlbumCount + expectedAlbumsCount, artistAlbumRepository.count(),
            "Artist-album relations should be created");

        // Verify album properties and attributes
        List<LastfmAlbum> savedAlbums = albumRepository.findAll();
        for (LastfmAlbum album : savedAlbums) {
            assertNotNull(album.getName(), "Album name should be set");
            assertNotNull(album.getUrl(), "Album URL should be set");
            
            // Find corresponding album in the DTO to get the original values
            var albumDto = dtoRoot.getTopAlbumsObject().getAlbums().stream()
                .filter(dto -> dto.getName().equals(album.getName()) && 
                       dto.getUrl().equals(album.getUrl()))
                .findFirst()
                .orElse(null);
                
            if (albumDto != null) {
                // Verify attributes using the test helper
                testHelper.verifyStringAttribute(album, LastfmAttribute.URL, albumDto.getUrl());
                testHelper.verifyNumericAttribute(album, LastfmAttribute.PLAY_COUNT, albumDto.getPlayCount());
                
                if (albumDto.getMbid() != null && !albumDto.getMbid().isEmpty()) {
                    testHelper.verifyStringAttribute(album, LastfmAttribute.MBID, albumDto.getMbid());
                }
            }
        }

        // Verify relation properties
        List<LastfmArtistAlbum> relations = artistAlbumRepository.findAll();
        for (LastfmArtistAlbum relation : relations) {
            assertEquals(sourceArtist.getId(), relation.getArtist().getId(),
                "Relation should reference the source artist");
            assertNotNull(relation.getAlbum(), "Relation should reference an album");
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getTopAlbumsObject().getAlbums().get(0).getArtist().getName())
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);

        // First processing
        processor.processResponse(apiResponse);

        // Record state after first processing
        long albumCountAfterFirstProcessing = albumRepository.count();
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        long relationCountAfterFirstProcessing = artistAlbumRepository.count();

        // when - process the same response again
        processor.processResponse(apiResponse);

        // then - counts should remain the same
        assertEquals(albumCountAfterFirstProcessing, albumRepository.count(),
            "Album count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
        assertEquals(relationCountAfterFirstProcessing, artistAlbumRepository.count(),
            "Relation count should remain the same after second processing");
    }

    @Test
    void process_shouldFilterAlbumsByPlayCount_whenThresholdIsSet() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getTopAlbumsObject().getAlbums().get(0).getArtist().getName())
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);

        // Set high threshold to filter some albums
        int threshold = 1000000; // High threshold to filter out some albums
        ReflectionTestUtils.setField(processor, "albumPlayCountThreshold", threshold);

        // Record initial state
        long initialAlbumCount = albumRepository.count();

        // Count how many albums should pass the threshold
        long expectedAlbumsCount = dtoRoot.getTopAlbumsObject().getAlbums().stream()
            .filter(album -> album.getPlayCount() >= threshold)
            .count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify only albums above threshold were processed
        assertEquals(initialAlbumCount + expectedAlbumsCount, albumRepository.count(),
            "Only albums above threshold should be created");

        // Verify all created albums have play count above threshold
        List<LastfmAlbum> savedAlbums = albumRepository.findAll();
        for (LastfmAlbum album : savedAlbums) {
            // Verify play count attribute using the test helper
            List<Long> playCountValues = attributeHistoryRepository.findAttributeValuesForEntity(
                LastfmAttribute.PLAY_COUNT, album.getType(), album.getId())
                .stream()
                .map(record -> record.getNumericValue())
                .toList();
                
            if (!playCountValues.isEmpty()) {
                assertTrue(playCountValues.get(0) >= threshold,
                    "Album play count should be above threshold");
            }
        }
    }

    @Test
    void process_shouldHandleEmptyAlbumsList() throws IOException {
        // given
        // Create empty albums response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
        ObjectNode topAlbumsNode = (ObjectNode) jsonNode.path("topalbums");
        topAlbumsNode.putArray("album"); // Replace albums with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);

        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();

        // Create API response with empty albums
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // Record initial state
        long initialAlbumCount = albumRepository.count();
        long initialRelationCount = artistAlbumRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then - no new entities should be created
        assertEquals(initialAlbumCount, albumRepository.count(),
            "No new albums should be created for empty response");
        assertEquals(initialRelationCount, artistAlbumRepository.count(),
            "No new relations should be created for empty response");
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
    void process_shouldSetArtistReferenceInAlbum_whenProcessingResponse() throws IOException {
        // given
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getTopAlbumsObject().getAlbums().get(0).getArtist().getName())
        );

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceArtist);

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify all albums have artist reference set
        List<LastfmAlbum> savedAlbums = albumRepository.findAll();
        assertFalse(savedAlbums.isEmpty(), "Albums should be created");

        for (LastfmAlbum album : savedAlbums) {
            assertNotNull(album.getArtist(), "Album should have artist reference set");
            assertEquals(sourceArtist.getId(), album.getArtist().getId(),
                "Album should reference the correct artist");
        }
    }

    @Test
    void process_shouldProcessArtistsFromAlbumDtos_beforeProcessingAlbums() throws IOException {
        // given
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();

        // Modify the response to use several artists
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(responseJsonString);
        ObjectNode topAlbumsNode = (ObjectNode) jsonNode.path("topalbums");
        ObjectNode firstAlbumNode = (ObjectNode) topAlbumsNode.path("album").get(0);
        ObjectNode artistNode = (ObjectNode) firstAlbumNode.path("artist");
        artistNode.put("name", UUID.randomUUID().toString());
        artistNode.put("mbid", UUID.randomUUID().toString());

        String modifiedResponseBody = objectMapper.writeValueAsString(jsonNode);

        // Create a new API response with the modified body
        LastfmApiResponse modifiedApiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponseBody, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // when
        processor.processResponse(modifiedApiResponse);

        // then
        // Verify artists were processed from DTOs
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        assertTrue(savedArtists.size() > 1, "Multiple artists should be processed");

        // Verify albums reference the correct artists
        List<LastfmAlbum> savedAlbums = albumRepository.findAll();
        assertFalse(savedAlbums.isEmpty(), "Albums should be created");

        List<LastfmArtistAlbum> savedRelations = artistAlbumRepository.findAll();
        assertFalse(savedRelations.isEmpty(), "Artist-Album relations should be created");

        for (LastfmAlbum album : savedAlbums) {
            assertNotNull(album.getArtist(), "Album should have artist reference set");
        }
    }

    @Test
    void process_shouldSkipBlacklistedAlbums_whenSomeAlbumsAreBlacklisted() throws Exception {
        // given
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Blacklist some albums from the response
        var albums = dtoRoot.getTopAlbumsObject().getAlbums();
        if (albums.size() > 1) {
            blacklistService.addToBlacklist(
                yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType.ALBUM, 
                albums.get(0).getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // Record initial state
        long initialAlbumCount = albumRepository.count();
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify some but not all albums were created
        long finalAlbumCount = albumRepository.count();
        assertTrue(finalAlbumCount > initialAlbumCount, "Some albums should be created");
        assertTrue(finalAlbumCount < initialAlbumCount + albums.size(), 
            "Not all albums should be created due to blacklist");

        // Verify artists and relationships were created
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be created");
        assertTrue(artistAlbumRepository.count() > 0, "Artist-album relationships should be created");
    }

    @Test
    void process_shouldHandleAllAlbumsBlacklisted_whenAllAlbumsAreBlacklisted() throws Exception {
        // given
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Blacklist ALL albums from the response
        var albums = dtoRoot.getTopAlbumsObject().getAlbums();
        for (var album : albums) {
            blacklistService.addToBlacklist(
                yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType.ALBUM, 
                album.getUrl());
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify no albums or relationships were created
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, albumRepository.count(), "No albums should be created");
        assertEquals(0, artistAlbumRepository.count(), "No artist-album relationships should be created");
    }

    @Test
    void process_shouldHandleAllArtistsBlacklisted_whenAllArtistsAreBlacklisted() throws Exception {
        // given
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Blacklist ALL artists from the response
        var albums = dtoRoot.getTopAlbumsObject().getAlbums();
        var uniqueArtistUrls = albums.stream()
            .map(album -> album.getArtist().getUrl())
            .distinct()
            .toList();
            
        for (String artistUrl : uniqueArtistUrls) {
            blacklistService.addToBlacklist(
                yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType.ARTIST, 
                artistUrl);
        }

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify no albums or relationships were created
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, albumRepository.count(), "No albums should be created");
        assertEquals(0, artistAlbumRepository.count(), "No artist-album relationships should be created");
    }

    @Test
    void process_shouldProcessNormally_whenNoEntitiesAreBlacklisted() throws Exception {
        // given
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // Record initial state
        long initialAlbumCount = albumRepository.count();
        long initialArtistCount = artistRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify albums and artists were created (those above threshold)
        assertTrue(albumRepository.count() > initialAlbumCount, "Albums should be created");
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be created");
        assertTrue(artistAlbumRepository.count() > 0, "Artist-album relationships should be created");
        assertTrue(attributeHistoryRepository.count() > 0, "Attribute history records should be created");
    }

    @Test
    void process_shouldFilterByPlayCountThreshold_whenAlbumsAreBelowThreshold() throws Exception {
        // given
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Modify response to have albums with low play counts
        ObjectMapper objectMapper = new ObjectMapper();
        ArtistTopAlbumsDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, ArtistTopAlbumsDtoRoot.class);
        
        // Set all albums to have low play counts (below threshold of 10000)
        modifiedDtoRoot.getTopAlbumsObject().getAlbums().forEach(album -> album.setPlayCount(5000L));
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify no albums were created due to low play counts
        assertEquals(1, artistRepository.count(), "Only source artist should exist");
        assertEquals(0, albumRepository.count(), "No albums should be created due to low play counts");
        assertEquals(0, artistAlbumRepository.count(), "No artist-album relationships should be created");
    }
}