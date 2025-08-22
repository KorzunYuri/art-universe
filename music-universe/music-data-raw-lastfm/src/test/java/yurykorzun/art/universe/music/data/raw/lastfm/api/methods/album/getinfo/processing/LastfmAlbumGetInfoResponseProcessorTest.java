package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.config.LastfmThresholdConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoDtoRoot;
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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmAlbumTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmAlbumTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmAlbumTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmAlbumTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.impl.LastfmTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmAlbumGetInfoResponseProcessor.class,
    LastfmAlbumGetInfoAlbumFactory.class,
    LastfmAlbumGetInfoTrackArtistFactory.class,
    LastfmAlbumGetInfoTagFactory.class,
    LastfmApiDtoProcessingService.class,
    // quality control
    BlacklistedEntityUrlService.class,
    DtoQualityService.class,
    LastfmThresholdConfig.class,
    // entities
    LastfmAlbumServiceImpl.class,
    LastfmArtistServiceImpl.class,
    LastfmTrackServiceImpl.class,
    LastfmTagServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmAlbumTrackServiceImpl.class,
    LastfmAlbumTagServiceImpl.class,
    LastfmArtistTrackServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmAlbumGetInfoResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmAlbumGetInfoResponseProcessor processor;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmAlbumTrackRepository albumTrackRepository;
    
    @Autowired
    private LastfmArtistTrackRepository artistTrackRepository;

    @Autowired
    private LastfmAlbumTagRepository albumTagRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "album.getInfo";
    private String responseJsonString;
    private AlbumGetInfoDtoRoot dtoRoot;

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
    private AlbumGetInfoDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, AlbumGetInfoDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing with a specific entity
     */
    private LastfmApiResponse createApiResponse(LastfmAlbum album) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.ALBUM_GET_INFO, album);
    }

    @Test
    void process_shouldUpdateExistingAlbum_whenAlbumGetInfoResponseProvided() throws Exception {
        // given
        // Create existing album with minimal data
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .mbid(null)
                    .playCount(0L)
                    .listenersCount(0)
                    .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingAlbum);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();
        long initialTagCount = tagRepository.count();
        long initialAlbumTrackCount = albumTrackRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        long initialAlbumTagCount = albumTagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify album was updated
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");
        
        // Verify album data was updated
        LastfmAlbum album = updatedAlbum.get();
        assertEquals(dtoRoot.getAlbum().getMbid(), album.getMbid(), "Album MBID should be updated");
        assertEquals(dtoRoot.getAlbum().getPlayCount(), album.getPlayCount(), "Album play count should be updated");
        assertEquals(dtoRoot.getAlbum().getListenersCount(), album.getListenersCount(), "Album listeners count should be updated");
        // verify non-changed fields
        assertEquals(dtoRoot.getAlbum().getName(), album.getName(), "Album name should remain the same");
        assertEquals(dtoRoot.getAlbum().getUrl(), album.getUrl(), "Album URL should remain the same");
        // Verify attribute history records
        testHelper.verifyNumericAttribute(album, LastfmAttribute.PLAY_COUNT, album.getPlayCount());
        testHelper.verifyNumericAttribute(album, LastfmAttribute.LISTENERS_COUNT, album.getListenersCount());
        
        // Verify approval status is preserved
        assertEquals(ApprovalStatus.APPROVED, album.getApprovalStatus(), "Album approval status should be preserved");
        
        // Verify artists were created
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be added to database");
        
        // Verify tracks were created
        int expectedTracksCount = dtoRoot.getAlbum().getTracksObject().getTracks().size();
        assertEquals(expectedTracksCount, trackRepository.count() - initialTrackCount, "All tracks should be saved to database");
        
        // Verify album-track relationships were created
        assertEquals(expectedTracksCount, albumTrackRepository.count() - initialAlbumTrackCount, 
            "Album-track relationships should be created");
        
        // Verify artist-track relationships were created
        assertEquals(expectedTracksCount, artistTrackRepository.count() - initialArtistTrackCount, 
            "Artist-track relationships should be created");
        
        // Verify tags were created
        int expectedTagsCount = dtoRoot.getAlbum().getTags().getTags().size();
        assertEquals(expectedTagsCount, tagRepository.count() - initialTagCount, "All tags should be saved to database");
        
        // Verify album-tag relationships were created
        assertEquals(expectedTagsCount, albumTagRepository.count() - initialAlbumTagCount, 
            "Album-tag relationships should be created");
        
        // Verify new attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");

        // Verify track positions in album-track relationships
        List<LastfmAlbumTrack> albumTracks = albumTrackRepository.findAll()
            .stream().filter(a -> a.getId() == album.getId())
            .toList();
        for (LastfmAlbumTrack albumTrack : albumTracks) {
            assertTrue(albumTrack.getPosition() > 0, "Track position should be set");
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws Exception {
        // given
        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getAlbum().getArtistName()));

        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder -> 
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .artist(existingArtist)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingAlbum);
        
        // First processing
        processor.processResponse(apiResponse);
        
        // Record counts after first processing
        long albumCount = albumRepository.count();
        long artistCount = artistRepository.count();
        long trackCount = trackRepository.count();
        long tagCount = tagRepository.count();
        long albumTrackCount = albumTrackRepository.count();
        long artistTrackCount = artistTrackRepository.count();
        long albumTagCount = albumTagRepository.count();
        long attributeCount = attributeHistoryRepository.count();
        
        // when
        // Process again
        processor.processResponse(apiResponse);
        
        // then
        // Verify counts remain the same
        assertEquals(albumCount, albumRepository.count(), 
            "Album count should remain the same after second processing");
        assertEquals(artistCount, artistRepository.count(), 
            "Artist count should remain the same after second processing");
        assertEquals(trackCount, trackRepository.count(), 
            "Track count should remain the same after second processing");
        assertEquals(tagCount, tagRepository.count(), 
            "Tag count should remain the same after second processing");
        assertEquals(albumTrackCount, albumTrackRepository.count(), 
            "Album-track relation count should remain the same after second processing");
        assertEquals(artistTrackCount, artistTrackRepository.count(), 
            "Artist-track relation count should remain the same after second processing");
        assertEquals(albumTagCount, albumTagRepository.count(), 
            "Album-tag relation count should remain the same after second processing");
        assertEquals(attributeCount, attributeHistoryRepository.count(),
            "Attribute history record count should remain the same after second processing");
    }

    @Test
    void process_shouldHandleEmptyTags_whenResponseHasNoTags() throws Exception {
        // given
        // Create a modified response with empty tags
        ObjectMapper objectMapper = new ObjectMapper();
        AlbumGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, AlbumGetInfoDtoRoot.class);
        modifiedDtoRoot.getAlbum().getTags().setTags(List.of()); // Set empty tags list
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);
        
        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder .name(dtoRoot.getAlbum().getArtistName()));
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .artist(existingArtist)
        );
        
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ALBUM_GET_INFO, existingAlbum);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify album was updated
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");
        
        // Verify tracks were created
        assertFalse(trackRepository.findAll().isEmpty(), "Tracks should be saved to database");
        
        // Verify album-track relationships were created
        assertFalse(albumTrackRepository.findAll().isEmpty(), "Album-track relationships should be created");
        
        // Verify no tags were created
        assertTrue(tagRepository.findAll().isEmpty(), "No tags should be created");
        
        // Verify no album-tag relationships were created
        assertTrue(albumTagRepository.findAll().isEmpty(), "No album-tag relationships should be created");
    }

    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getAlbum().getArtistName()));
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder.artist(existingArtist));
        
        String invalidJson = "{}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.ALBUM_GET_INFO, existingAlbum);
        
        // when/then
        assertThrows(RuntimeException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created except the existing album
        assertEquals(1, albumRepository.count(), "Only the existing album should be in database");
        assertEquals(0, trackRepository.count(), "No tracks should be created");
        assertEquals(0, tagRepository.count(), "No tags should be created");
    }

    @Test
    void process_shouldThrowException_whenAlbumNotFound() {
        // given
        // Create a non-existent album ID
        long nonExistentAlbumId = 999L;
        
        // Create API call with non-existent album ID
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall(builder ->
            builder.type(LastfmApiCallType.ALBUM_GET_INFO)
                   .entityType(LastfmEntityType.ALBUM)
                   .entityId(nonExistentAlbumId)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, apiCall);
        
        // when/then
        assertThrows(IllegalStateException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when album not found");
    }

    @Test
    void process_shouldHandleNoTracks_whenResponseHasNoTracksObject() throws Exception {
        // given
        // Create a modified response with no tracks
        ObjectMapper objectMapper = new ObjectMapper();
        AlbumGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, AlbumGetInfoDtoRoot.class);
        modifiedDtoRoot.getAlbum().setTracksObject(null); // Remove tracks object completely
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);

        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getAlbum().getArtistName()));
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .artist(existingArtist)
        );

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ALBUM_GET_INFO, existingAlbum);

        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialAlbumTrackCount = albumTrackRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify album was updated
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");

        // Verify album data was updated
        LastfmAlbum album = updatedAlbum.get();
        assertEquals(modifiedDtoRoot.getAlbum().getPlayCount(), album.getPlayCount(), "Album play count should be updated");
        assertEquals(modifiedDtoRoot.getAlbum().getListenersCount(), album.getListenersCount(), "Album listeners count should be updated");

        // Verify no tracks were created
        assertEquals(initialTrackCount, trackRepository.count(), "No tracks should be created");

        // Verify no album-track relationships were created
        assertEquals(initialAlbumTrackCount, albumTrackRepository.count(), "No album-track relationships should be created");

        // Verify no artist-track relationships were created
        assertEquals(initialArtistTrackCount, artistTrackRepository.count(), "No artist-track relationships should be created");

        // Verify tags were still processed
        assertFalse(tagRepository.findAll().isEmpty(), "Tags should still be processed");
        assertFalse(albumTagRepository.findAll().isEmpty(), "Album-tag relationships should still be created");
    }

    @Test
    void process_shouldHandleNoTagsAndNoTracks_whenResponseHasNeither() throws Exception {
        // given
        // Create a modified response with no tracks and no tags
        ObjectMapper objectMapper = new ObjectMapper();
        AlbumGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, AlbumGetInfoDtoRoot.class);
        modifiedDtoRoot.getAlbum().setTracksObject(null); // Remove tracks object completely
        modifiedDtoRoot.getAlbum().setTags(null); // Remove tags object completely
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);

        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getAlbum().getArtistName()));
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .artist(existingArtist)
        );

        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.ALBUM_GET_INFO, existingAlbum);

        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialTagCount = tagRepository.count();
        long initialAlbumTrackCount = albumTrackRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        long initialAlbumTagCount = albumTagRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify album was updated
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");

        // Verify album data was updated
        LastfmAlbum album = updatedAlbum.get();
        assertEquals(modifiedDtoRoot.getAlbum().getPlayCount(), album.getPlayCount(), "Album play count should be updated");
        assertEquals(modifiedDtoRoot.getAlbum().getListenersCount(), album.getListenersCount(), "Album listeners count should be updated");

        // Verify no tracks were created
        assertEquals(initialTrackCount, trackRepository.count(), "No tracks should be created");

        // Verify no album-track relationships were created
        assertEquals(initialAlbumTrackCount, albumTrackRepository.count(), "No album-track relationships should be created");

        // Verify no artist-track relationships were created
        assertEquals(initialArtistTrackCount, artistTrackRepository.count(), "No artist-track relationships should be created");

        // Verify no tags were created
        assertEquals(initialTagCount, tagRepository.count(), "No tags should be created");

        // Verify no album-tag relationships were created
        assertEquals(initialAlbumTagCount, albumTagRepository.count(), "No album-tag relationships should be created");
    }

    @Test
    void process_shouldSkipTrackProcessing_whenAllArtistsAreBlacklisted() throws Exception {
        // given
        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getAlbum().getArtistName()));
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .artist(existingArtist)
        );

        // Blacklist all artists from the response
        dtoRoot.getAlbum().getTracksObject().getTracks().stream()
            .map(track -> track.getArtist().getUrl())
            .distinct()
            .forEach(artistUrl -> blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artistUrl));

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingAlbum);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();
        long initialAlbumTrackCount = albumTrackRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify album was still updated (album processing is not affected by artist blacklist)
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");

        // Verify no artists or tracks were created due to blacklist
        assertEquals(initialArtistCount, artistRepository.count(), "No artists should be created");
        assertEquals(initialTrackCount, trackRepository.count(), "No tracks should be created");
        assertEquals(initialAlbumTrackCount, albumTrackRepository.count(), "No album-track relationships should be created");
        assertEquals(initialArtistTrackCount, artistTrackRepository.count(), "No artist-track relationships should be created");

        // Verify tags were still processed (tags are not affected by artist blacklist)
        assertTrue(tagRepository.count() > 0, "Tags should still be processed");
        assertTrue(albumTagRepository.count() > 0, "Album-tag relationships should still be created");
    }

    @Test
    void process_shouldProcessPartiallyBlacklistedArtists() throws Exception {
        // given
        // Create existing album
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name(dtoRoot.getAlbum().getArtistName()));
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .artist(existingArtist)
        );

        // Get all unique artist URLs from the response
        var artistUrls = dtoRoot.getAlbum().getTracksObject().getTracks().stream()
            .map(track -> track.getArtist().getUrl())
            .distinct()
            .toList();

        // Blacklist only the first artist (if there are multiple)
        if (artistUrls.size() > 1) {
            blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artistUrls.get(0));
        } else {
            // If there's only one artist, skip this test
            return;
        }

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingAlbum);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify some artists were created (non-blacklisted ones)
        assertTrue(artistRepository.count() > initialArtistCount, "Some artists should be created");
        assertTrue(artistRepository.count() < initialArtistCount + artistUrls.size(),
            "Not all artists should be created due to blacklist");

        // Verify some tracks were created
        assertTrue(trackRepository.count() > initialTrackCount, "Some tracks should be created");

        // Verify relationships were created
        assertTrue(albumTrackRepository.count() > 0, "Some album-track relationships should be created");
        assertTrue(artistTrackRepository.count() > 0, "Some artist-track relationships should be created");
    }

    @Test
    void process_shouldProcessNormally_whenNoArtistsAreBlacklisted() throws Exception {
        // given
        // Create existing album
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Create API response (no blacklisting)
        LastfmApiResponse apiResponse = createApiResponse(existingAlbum);

        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialTrackCount = trackRepository.count();
        long initialTagCount = tagRepository.count();
        long initialAlbumTrackCount = albumTrackRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        long initialAlbumTagCount = albumTagRepository.count();

        // when
        processor.processResponse(apiResponse);

        // then
        // Verify album was updated
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");

        // Verify artists were created
        assertTrue(artistRepository.count() > initialArtistCount, "Artists should be added to database");

        // Verify tracks were created
        int expectedTracksCount = dtoRoot.getAlbum().getTracksObject().getTracks().size();
        assertEquals(expectedTracksCount, trackRepository.count() - initialTrackCount, "All tracks should be saved to database");

        // Verify album-track relationships were created
        assertEquals(expectedTracksCount, albumTrackRepository.count() - initialAlbumTrackCount,
            "Album-track relationships should be created");

        // Verify artist-track relationships were created
        assertEquals(expectedTracksCount, artistTrackRepository.count() - initialArtistTrackCount,
            "Artist-track relationships should be created");

        // Verify tags were created
        int expectedTagsCount = dtoRoot.getAlbum().getTags().getTags().size();
        assertEquals(expectedTagsCount, tagRepository.count() - initialTagCount, "All tags should be saved to database");

        // Verify album-tag relationships were created
        assertEquals(expectedTagsCount, albumTagRepository.count() - initialAlbumTagCount,
            "Album-tag relationships should be created");
    }

    @Test
    void process_shouldHandleEmptyArtistList_whenAllArtistsFiltered() throws Exception {
        // given
        // Create existing album
        LastfmAlbum existingAlbum = consistencyHelper.createAndSaveAlbum(builder ->
            builder .name(dtoRoot.getAlbum().getName())
                    .url(dtoRoot.getAlbum().getUrl())
                    .approvalStatus(ApprovalStatus.APPROVED)
        );

        // Blacklist ALL artists from the response
        dtoRoot.getAlbum().getTracksObject().getTracks().stream()
            .map(track -> track.getArtist().getUrl())
            .distinct()
            .forEach(artistUrl -> blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artistUrl));

        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingAlbum);

        // when - should not throw exception
        assertDoesNotThrow(() -> processor.processResponse(apiResponse));

        // then
        // Verify album was still updated
        Optional<LastfmAlbum> updatedAlbum = albumRepository.findById(existingAlbum.getId());
        assertTrue(updatedAlbum.isPresent(), "Album should still exist in database");

        // Verify no tracks or artists were created
        assertEquals(0, artistRepository.count(), "No artists should be created");
        assertEquals(0, trackRepository.count(), "No tracks should be created");

        // Verify tags were still processed
        assertTrue(tagRepository.count() > 0, "Tags should still be processed");
    }
}
