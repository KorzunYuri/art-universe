package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl.LastfmAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    LastfmArtistTopAlbumsResponseProcessor.class,
    LastfmArtistTopAlbumsAlbumFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmAlbumServiceImpl.class,
    LastfmArtistServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmArtistAlbumServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
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
    private LastfmArtistAlbumRepository artistAlbumRepository;

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistTopAlbumsResponseProvided() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopAlbums");
        ArtistTopAlbumsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopAlbumsObject().getAlbums().get(0).getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);
        
        // Set threshold to 0 to process all albums
        ReflectionTestUtils.setField(processor, "albumPlayCountThreshold", 0);
        
        // Record initial state
        long initialAlbumCount = albumRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistAlbumCount = artistAlbumRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new albums were created
        int expectedAlbumsCount = dtoRoot.getTopAlbumsObject().getAlbums().size();
        assertEquals(initialAlbumCount + expectedAlbumsCount, albumRepository.count(), 
            "New albums should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-album relations were created
        assertEquals(initialArtistAlbumCount + expectedAlbumsCount, artistAlbumRepository.count(), 
            "Artist-album relations should be created");
        
        // Verify album properties
        List<LastfmAlbum> savedAlbums = albumRepository.findAll();
        for (LastfmAlbum album : savedAlbums) {
            assertNotNull(album.getName(), "Album name should be set");
            assertNotNull(album.getUrl(), "Album URL should be set");
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
    void process_shouldFilterAlbumsByPlayCount_whenThresholdIsSet() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopAlbums");
        ArtistTopAlbumsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source artist
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(dtoRoot.getTopAlbumsObject().getAlbums().get(0).getArtist().getName())
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);
        
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
        List<LastfmAttributeHistoryRecord> playCountAttributes = attributeHistoryRepository.findAll().stream()
            .filter(attr -> attr.getAttribute().getName().equals("play_count"))
            .toList();
        
        for (LastfmAttributeHistoryRecord attr : playCountAttributes) {
            assertTrue(attr.getIntValue() >= threshold, 
                "All created albums should have play count above threshold");
        }
    }

    @Test
    void process_shouldThrowException_whenSourceArtistNotFound() throws IOException {
        // given
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopAlbums");

        // Create source artist first (needed for API call creation)
        LastfmArtist sourceArtist = consistencyHelper.createAndSaveArtist();

        // Create API response with the artist
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.ARTIST_TOP_ALBUMS, sourceArtist);

        // Now delete the artist to simulate non-existent artist
        artistRepository.delete(sourceArtist);

        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source artist not found");
    }

    private ArtistTopAlbumsDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistTopAlbumsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
