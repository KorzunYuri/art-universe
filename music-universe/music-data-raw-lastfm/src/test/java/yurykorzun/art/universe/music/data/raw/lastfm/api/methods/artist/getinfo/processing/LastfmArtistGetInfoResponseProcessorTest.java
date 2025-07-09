package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistsRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    LastfmArtistGetInfoResponseProcessor.class,
    LastfmArtistGetInfoArtistFactory.class,
    LastfmArtistGetInfoSimilarArtistFactory.class,
    LastfmArtistGetInfoTagFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmArtistServiceImpl.class,
    LastfmTagServiceImpl.class,
    LastfmArtistsRelationServiceImpl.class,
    LastfmArtistTagServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
})
class LastfmArtistGetInfoResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistGetInfoResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmArtistTagRepository artistTagRepository;

    @Autowired
    private LastfmArtistsRelationRepository artistsRelationRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenArtistGetInfoResponseAndNoExistingArtistProvided() throws Exception {
        // given
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getInfo");
        ArtistGetInfoDtoRoot dtoRoot = parseResponse(responseJsonString);
        String artistName = dtoRoot.getArtist().getName();
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_INFO);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was created
        Optional<LastfmArtist> savedArtist = artistRepository.findByName(artistName);
        assertTrue(savedArtist.isPresent(), "Artist should be saved to database");
        
        // Verify tags were created
        int expectedTagsCount = dtoRoot.getArtist().getTagsObject().getTags().size();
        List<LastfmTag> savedTags = tagRepository.findAll();
        assertEquals(expectedTagsCount, savedTags.size(), "All tags should be saved to database");
        
        // Verify artist-tag relations were created
        List<LastfmArtistTag> artistTagRelations = artistTagRepository.findAll();
        assertEquals(expectedTagsCount, artistTagRelations.size(), "Artist-tag relations should be created");
        
        // Verify attribute history records were created
        List<LastfmAttributeHistoryRecord> attributeRecords = attributeHistoryRepository.findAll();
        assertFalse(attributeRecords.isEmpty(), "Attribute history records should be created");
        
        // Check specific artist attributes
        LastfmArtist artist = savedArtist.get();
        assertEquals(artistName, artist.getName(), "Artist name should match");
        assertNotNull(artist.getUrl(), "Artist URL should be set");
        assertNotNull(artist.getListenersCount(), "Artist listeners count should be set");
        assertNotNull(artist.getPlayCount(), "Artist play count should be set");
    }

    @Test
    void process_shouldUpdateExistingArtist_whenArtistGetInfoResponseAndExistingArtistProvided() throws Exception {
        // given
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getInfo");
        ArtistGetInfoDtoRoot dtoRoot = parseResponse(responseJsonString);
        String artistName = dtoRoot.getArtist().getName();
        
        // Create existing artist with minimal data
        LastfmArtist existingArtist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name(artistName)
                   .url("http://old-url.com")
                   .listenersCount(100)
                   .playCount(200)
                   .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseJsonString, LastfmApiCallType.ARTIST_GET_INFO, existingArtist);
        
        // Record initial state
        long initialTagCount = tagRepository.count();
        long initialArtistTagCount = artistTagRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify artist was updated
        Optional<LastfmArtist> updatedArtist = artistRepository.findById(existingArtist.getId());
        assertTrue(updatedArtist.isPresent(), "Artist should still exist in database");
        
        // Verify artist data was updated
        LastfmArtist artist = updatedArtist.get();
        assertEquals(artistName, artist.getName(), "Artist name should remain the same");
        assertNotEquals("http://old-url.com", artist.getUrl(), "Artist URL should be updated");
        assertNotEquals(100, artist.getListenersCount(), "Artist listeners count should be updated");
        assertNotEquals(200, artist.getPlayCount(), "Artist play count should be updated");
        
        // Verify new tags were created
        int expectedTagsCount = dtoRoot.getArtist().getTagsObject().getTags().size();
        assertTrue(tagRepository.count() > initialTagCount, "New tags should be added to database");
        
        // Verify new artist-tag relations were created
        assertTrue(artistTagRepository.count() > initialArtistTagCount, 
            "New artist-tag relations should be created");
        
        // Verify new attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
    }

    private ArtistGetInfoDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, ArtistGetInfoDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
