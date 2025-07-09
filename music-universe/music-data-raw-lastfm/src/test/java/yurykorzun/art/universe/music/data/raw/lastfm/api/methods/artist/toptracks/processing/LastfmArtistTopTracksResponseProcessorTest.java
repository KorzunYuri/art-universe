package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.impl.LastfmTrackServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    LastfmArtistTopTracksResponseProcessor.class,
    LastfmArtistTopTracksTrackFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmTrackServiceImpl.class,
    LastfmArtistServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmArtistTrackServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
})
class LastfmArtistTopTracksResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopTracksResponseProcessor processor;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistTrackRepository artistTrackRepository;

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistTopTracksResponseProvided() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTracks");
        ArtistTopTracksDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getRootObject().getArtistMetadata().getArtistName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_TRACKS, sourceArtist);
        
        // Set threshold to 0 to process all tracks
        ReflectionTestUtils.setField(processor, "trackListenersThreshold", 0);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new tracks were created
        int expectedTracksCount = dtoRoot.getRootObject().getTracks().size();
        assertEquals(initialTrackCount + expectedTracksCount, trackRepository.count(), 
            "New tracks should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-track relations were created
        assertEquals(initialArtistTrackCount + expectedTracksCount, artistTrackRepository.count(), 
            "Artist-track relations should be created");
        
        // Verify track properties
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        for (LastfmTrack track : savedTracks) {
            assertNotNull(track.getName(), "Track name should be set");
            assertNotNull(track.getUrl(), "Track URL should be set");
        }
        
        // Verify relation properties
        List<LastfmArtistTrack> relations = artistTrackRepository.findAll();
        for (LastfmArtistTrack relation : relations) {
            assertEquals(sourceArtist.getId(), relation.getArtist().getId(), 
                "Relation should reference the source artist");
            assertNotNull(relation.getTrack(), "Relation should reference a track");
        }
    }

    @Test
    void process_shouldFilterTracksByListenersCount_whenThresholdIsSet() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTracks");
        ArtistTopTracksDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getRootObject().getArtistMetadata().getArtistName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_TRACKS, sourceArtist);
        
        // Set high threshold to filter some tracks
        int threshold = 1000000; // High threshold to filter out some tracks
        ReflectionTestUtils.setField(processor, "trackListenersThreshold", threshold);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        
        // Count how many tracks should pass the threshold
        long expectedTracksCount = dtoRoot.getRootObject().getTracks().stream()
            .filter(track -> track.getListenersCount() >= threshold)
            .count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify only tracks above threshold were processed
        assertEquals(initialTrackCount + expectedTracksCount, trackRepository.count(), 
            "Only tracks above threshold should be created");
        
        // Verify all created tracks have listeners count above threshold
        List<LastfmAttributeHistoryRecord> listenersCountAttributes = attributeHistoryRepository.findAll().stream()
            .filter(attr -> attr.getAttribute().getName().equals("listeners_count"))
            .toList();
        
        for (LastfmAttributeHistoryRecord attr : listenersCountAttributes) {
            assertTrue(attr.getIntValue() >= threshold, 
                "All created tracks should have listeners count above threshold");
        }
    }

    @Test
    void process_shouldThrowException_whenSourceArtistNotFound() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTracks");
        
        // Create source artist first (needed for API call creation)
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();
        
        // Create API response with the artist
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_TRACKS, sourceArtist);
        
        // Now delete the artist to simulate non-existent artist
        artistRepository.delete(sourceArtist);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source artist not found");
    }

    private ArtistTopTracksDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistTopTracksDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
