package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.processing;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto.TrackGetInfoDtoRoot;
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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmTrackTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmAlbumTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmTrackTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmAlbumTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmTrackTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
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
    LastfmTrackGetInfoResponseProcessor.class,
    LastfmTrackGetInfoArtistFactory.class,
    LastfmTrackGetInfoAlbumFactory.class,
    LastfmTrackGetInfoTagFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmTrackServiceImpl.class,
    LastfmArtistServiceImpl.class,
    LastfmAlbumServiceImpl.class,
    LastfmTagServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmAlbumTrackServiceImpl.class,
    LastfmTrackTagServiceImpl.class,
    LastfmArtistTrackServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmTrackGetInfoResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTrackGetInfoResponseProcessor processor;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmAlbumTrackRepository albumTrackRepository;
    
    @Autowired
    private LastfmArtistTrackRepository artistTrackRepository;

    @Autowired
    private LastfmTrackTagRepository trackTagRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private static final String TEST_RESPONSE_KEY = "track.getInfo";
    private String responseJsonString;
    private TrackGetInfoDtoRoot dtoRoot;

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
    private TrackGetInfoDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, TrackGetInfoDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse() {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.TRACK_GET_INFO);
    }

    /**
     * Helper method to create API response for testing with a specific entity
     */
    private LastfmApiResponse createApiResponse(LastfmTrack track) {
        return consistencyHelper.createAndSaveApiResponse(responseJsonString, LastfmApiCallType.TRACK_GET_INFO, track);
    }

    @Test
    void process_shouldCreateNewRecords_whenTrackGetInfoResponseAndNoExistingTrackProvided() throws Exception {
        // given
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify track was created
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        assertEquals(1, savedTracks.size(), "Track should be saved to database");
        
        LastfmTrack track = savedTracks.get(0);
        assertEquals(dtoRoot.getTrack().getName(), track.getName(), "Track name should match");
        assertEquals(dtoRoot.getTrack().getMbid(), track.getMbid(), "Track MBID should match");
        assertEquals(dtoRoot.getTrack().getUrl(), track.getUrl(), "Track URL should match");
        assertEquals(dtoRoot.getTrack().getDuration(), track.getDuration(), "Track duration should match");
        
        // Verify artist was created
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        assertEquals(1, savedArtists.size(), "Artist should be saved to database");
        
        LastfmArtist artist = savedArtists.get(0);
        assertEquals(dtoRoot.getTrack().getArtist().getName(), artist.getName(), "Artist name should match");
        
        // Verify artist is set on track
        assertNotNull(track.getArtist(), "Track should have artist reference");
        assertEquals(artist.getId(), track.getArtist().getId(), "Track artist ID should match created artist ID");
        
        // Verify artist-track relationship was created
        List<LastfmArtistTrack> artistTracks = artistTrackRepository.findAll();
        assertFalse(artistTracks.isEmpty(), "Artist-track relationship should be created");
        assertEquals(artist.getId(), artistTracks.get(0).getArtist().getId(), "Artist ID should match");
        assertEquals(track.getId(), artistTracks.get(0).getTrack().getId(), "Track ID should match");
        
        // Verify album was created if present
        if (dtoRoot.getTrack().getAlbum() != null) {
            List<LastfmAlbum> savedAlbums = albumRepository.findAll();
            assertFalse(savedAlbums.isEmpty(), "Album should be saved to database");
            
            LastfmAlbum album = savedAlbums.get(0);
            assertEquals(dtoRoot.getTrack().getAlbum().getName(), album.getName(), "Album name should match");
            
            // Verify album-track relationship was created
            List<LastfmAlbumTrack> albumTracks = albumTrackRepository.findAll();
            assertFalse(albumTracks.isEmpty(), "Album-track relationship should be created");
            assertEquals(album.getId(), albumTracks.get(0).getAlbum().getId(), "Album ID should match");
            assertEquals(track.getId(), albumTracks.get(0).getTrack().getId(), "Track ID should match");
            
            // Verify position is set if available
            if (dtoRoot.getTrack().getAlbum().getPosition() != null) {
                assertEquals(dtoRoot.getTrack().getAlbum().getPosition(), albumTracks.get(0).getPosition(), 
                    "Album track position should match");
            }
        }
        
        // Verify tags were created
        int expectedTagsCount = dtoRoot.getTrack().getTopTags().getTags().size();
        List<LastfmTag> savedTags = tagRepository.findAll();
        assertEquals(expectedTagsCount, savedTags.size(), "All tags should be saved to database");
        
        // Verify track-tag relationships were created
        List<LastfmTrackTag> trackTags = trackTagRepository.findAll();
        assertEquals(expectedTagsCount, trackTags.size(), "Track-tag relationships should be created");
        
        // Verify attribute history records were created
        assertFalse(attributeHistoryRepository.findAll().isEmpty(), "Attribute history records should be created");
        
        // Verify specific track attributes
        testHelper.verifyStringAttribute(track, LastfmAttribute.URL, track.getUrl());
        testHelper.verifyStringAttribute(track, LastfmAttribute.MBID, track.getMbid());
        testHelper.verifyNumericAttribute(track, LastfmAttribute.DURATION, track.getDuration());
        testHelper.verifyNumericAttribute(track, LastfmAttribute.PLAY_COUNT, dtoRoot.getTrack().getPlaycount());
        testHelper.verifyNumericAttribute(track, LastfmAttribute.LISTENERS_COUNT, dtoRoot.getTrack().getListeners());
    }

    @Test
    void process_shouldUpdateExistingTrack_whenTrackGetInfoResponseAndExistingTrackProvided() throws Exception {
        // given
        // Create existing track with minimal data
        LastfmTrack existingTrack = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name(dtoRoot.getTrack().getName())
                   .url(dtoRoot.getTrack().getUrl())
                   .duration(0)
                   .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(existingTrack);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialTrackTagCount = trackTagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify track was updated
        Optional<LastfmTrack> updatedTrack = trackRepository.findById(existingTrack.getId());
        assertTrue(updatedTrack.isPresent(), "Track should still exist in database");
        
        // Verify track data was updated
        LastfmTrack track = updatedTrack.get();
        assertEquals(dtoRoot.getTrack().getName(), track.getName(), "Track name should remain the same");
        assertEquals(dtoRoot.getTrack().getMbid(), track.getMbid(), "Track MBID should be updated");
        assertEquals(dtoRoot.getTrack().getUrl(), track.getUrl(), "Track URL should remain the same");
        assertEquals(dtoRoot.getTrack().getDuration(), track.getDuration(), "Track duration should be updated");
        
        // Verify approval status is preserved
        assertEquals(ApprovalStatus.APPROVED, track.getApprovalStatus(), "Track approval status should be preserved");
        
        // Verify artist-track relationship was created
        List<LastfmArtistTrack> artistTracks = artistTrackRepository.findAll();
        assertFalse(artistTracks.isEmpty(), "Artist-track relationship should be created");
        
        // Verify new tags were created
        int expectedTagsCount = dtoRoot.getTrack().getTopTags().getTags().size();
        assertTrue(tagRepository.count() > initialTagCount, "New tags should be added to database");
        
        // Verify new track-tag relations were created
        assertTrue(trackTagRepository.count() > initialTrackTagCount, 
            "New track-tag relations should be created");
        
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
        long trackCount = trackRepository.count();
        long artistCount = artistRepository.count();
        long albumCount = albumRepository.count();
        long tagCount = tagRepository.count();
        long trackTagCount = trackTagRepository.count();
        long albumTrackCount = albumTrackRepository.count();
        long artistTrackCount = artistTrackRepository.count();
        long attributeCount = attributeHistoryRepository.count();
        
        // Process again
        processor.processResponse(apiResponse);
        
        // then
        // Verify counts remain the same
        assertEquals(trackCount, trackRepository.count(), 
            "Track count should remain the same after second processing");
        assertEquals(artistCount, artistRepository.count(), 
            "Artist count should remain the same after second processing");
        assertEquals(albumCount, albumRepository.count(), 
            "Album count should remain the same after second processing");
        assertEquals(tagCount, tagRepository.count(), 
            "Tag count should remain the same after second processing");
        assertEquals(trackTagCount, trackTagRepository.count(), 
            "Track-tag relation count should remain the same after second processing");
        assertEquals(albumTrackCount, albumTrackRepository.count(), 
            "Album-track relation count should remain the same after second processing");
        assertEquals(artistTrackCount, artistTrackRepository.count(), 
            "Artist-track relation count should remain the same after second processing");
        assertEquals(attributeCount, attributeHistoryRepository.count(),
            "Attribute history record count should remain the same after second processing");
    }

    @Test
    void process_shouldHandleEmptyTags_whenResponseHasNoTags() throws Exception {
        // given
        // Create a modified response with empty tags
        ObjectMapper objectMapper = new ObjectMapper();
        TrackGetInfoDtoRoot modifiedDtoRoot = objectMapper.readValue(responseJsonString, TrackGetInfoDtoRoot.class);
        modifiedDtoRoot.getTrack().getTopTags().setTags(List.of()); // Set empty tags list
        String modifiedResponse = objectMapper.writeValueAsString(modifiedDtoRoot);
        
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            modifiedResponse, LastfmApiCallType.TRACK_GET_INFO);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify track was created
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        assertEquals(1, savedTracks.size(), "Track should be saved to database");
        
        // Verify artist was created
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        assertEquals(1, savedArtists.size(), "Artist should be saved to database");
        
        // Verify artist-track relationship was created
        List<LastfmArtistTrack> artistTracks = artistTrackRepository.findAll();
        assertEquals(1, artistTracks.size(), "Artist-track relationship should be created");
        
        // Verify no tags were created
        List<LastfmTag> savedTags = tagRepository.findAll();
        assertTrue(savedTags.isEmpty(), "No tags should be created");
        
        // Verify no track-tag relations were created
        List<LastfmTrackTag> trackTags = trackTagRepository.findAll();
        assertTrue(trackTags.isEmpty(), "No track-tag relations should be created");
    }

    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        String invalidJson = "{\"track\": {\"invalid\": true}}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.TRACK_GET_INFO);
        
        // when/then
        assertThrows(RuntimeException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(0, trackRepository.count(), "No tracks should be created");
        assertEquals(0, artistRepository.count(), "No artists should be created");
        assertEquals(0, albumRepository.count(), "No albums should be created");
        assertEquals(0, tagRepository.count(), "No tags should be created");
    }
}
