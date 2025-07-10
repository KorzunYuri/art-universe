package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.LastfmApiResponseProcessorTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    // processing
    LastfmTagTopTracksResponseProcessor.class,
    LastfmTagTopTracksArtistFactory.class,
    LastfmTagTopTracksTrackFactory.class,
    LastfmApiDtoProcessingService.class,
    // entities
    LastfmArtistServiceImpl.class,
    LastfmTrackServiceImpl.class,
    LastfmTagServiceImpl.class,
    // attributes
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    // relations
    LastfmArtistTrackServiceImpl.class,
    // helpers
    LastfmApiResponseProcessorTestHelper.class
})
class LastfmTagTopTracksResponseProcessorTest extends JpaOnlyTest {
    
    @Autowired
    private DbConsistencyHelper consistencyHelper;
    
    @Autowired
    private LastfmTagTopTracksResponseProcessor processor;

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistTrackRepository artistTrackRepository;
    
    @Autowired
    private LastfmApiResponseProcessorTestHelper testHelper;

    private TagTopTracksDtoRoot dtoRoot;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws IOException {
        consistencyHelper.cleanup();
        
        // Parse test data once for all tests
        dtoRoot = parseResponse(TEST_DTO_ROOT);
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    /**
     * Helper method to parse JSON response into DTO
     */
    private TagTopTracksDtoRoot parseResponse(String responseString) {
        try {
            return objectMapper.readValue(responseString, TagTopTracksDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    /**
     * Helper method to create API response for testing
     */
    private LastfmApiResponse createApiResponse(LastfmTag tag) {
        return consistencyHelper.createAndSaveApiResponse(TEST_DTO_ROOT, LastfmApiCallType.TAG_TOP_TRACKS, tag);
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopTracksResponseProvided() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTrackCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new tracks were created
        int expectedTracksCount = dtoRoot.getRootObject().getTracks().size();
        assertEquals(initialTrackCount + expectedTracksCount, trackRepository.count(), 
            "New tracks should be created");
        
        // Verify new artists were created (should be 2 unique artists)
        int expectedArtistsCount = 2; // Nirvana and Radiohead
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-track relations were created
        assertEquals(initialArtistTrackCount + expectedTracksCount, artistTrackRepository.count(), 
            "Artist-track relations should be created");
        
        // Verify track properties and attributes
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        for (LastfmTrack track : savedTracks) {
            assertNotNull(track.getName(), "Track name should be set");
            assertNotNull(track.getUrl(), "Track URL should be set");
            
            // Find corresponding track in the DTO to get the original values
            var trackDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getName().equals(track.getName()) && 
                       dto.getUrl().equals(track.getUrl()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Saved track doesn't correspond to any DTO"));

            // Verify attributes using the test helper
            testHelper.verifyStringAttribute(track, LastfmAttribute.URL, trackDto.getUrl());
            testHelper.verifyNumericAttribute(track, LastfmAttribute.DURATION, trackDto.getDuration());

            if (trackDto.getMbid() != null && !trackDto.getMbid().isEmpty()) {
                testHelper.verifyStringAttribute(track, LastfmAttribute.MBID, trackDto.getMbid());
            }
        }
        
        // Verify artist properties and attributes
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            assertNotNull(artist.getUrl(), "Artist URL should be set");
            
            // Find corresponding artist in the DTO to get the original values
            var artistDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getArtist().getName().equals(artist.getName()))
                .findFirst()
                .map(dto -> dto.getArtist())
                .orElse(null);
                
            if (artistDto != null) {
                // Verify attributes using the test helper
                testHelper.verifyStringAttribute(artist, LastfmAttribute.URL, artistDto.getUrl());
                
                if (artistDto.getMbid() != null && !artistDto.getMbid().isEmpty()) {
                    testHelper.verifyStringAttribute(artist, LastfmAttribute.MBID, artistDto.getMbid());
                }
            }
        }
        
        // Verify relation properties
        List<LastfmArtistTrack> relations = artistTrackRepository.findAll();
        for (LastfmArtistTrack relation : relations) {
            assertNotNull(relation.getArtist(), "Relation should reference an artist");
            assertNotNull(relation.getTrack(), "Relation should reference a track");
            
            // Verify the artist-track relationship is correct
            var trackDto = dtoRoot.getRootObject().getTracks().stream()
                .filter(dto -> dto.getName().equals(relation.getTrack().getName()))
                .findFirst()
                .orElse(null);
                
            if (trackDto != null) {
                assertEquals(trackDto.getArtist().getName(), relation.getArtist().getName(),
                    "Track should be associated with the correct artist");
            }
        }
    }

    @Test
    void process_shouldBeIdempotent_whenProcessingSameResponseTwice() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // First processing
        processor.processResponse(apiResponse);
        
        // Record state after first processing
        long trackCountAfterFirstProcessing = trackRepository.count();
        long artistCountAfterFirstProcessing = artistRepository.count();
        long attributeCountAfterFirstProcessing = attributeHistoryRepository.count();
        long relationCountAfterFirstProcessing = artistTrackRepository.count();
        
        // when - process the same response again
        processor.processResponse(apiResponse);
        
        // then - counts should remain the same
        assertEquals(trackCountAfterFirstProcessing, trackRepository.count(),
            "Track count should remain the same after second processing");
        assertEquals(artistCountAfterFirstProcessing, artistRepository.count(),
            "Artist count should remain the same after second processing");
        assertEquals(attributeCountAfterFirstProcessing, attributeHistoryRepository.count(),
            "Attribute count should remain the same after second processing");
        assertEquals(relationCountAfterFirstProcessing, artistTrackRepository.count(),
            "Relation count should remain the same after second processing");
    }

    @Test
    void process_shouldThrowException_whenSourceTagNotFound() throws IOException {
        // given
        // Create source tag first (needed for API call creation)
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with the tag
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // Now delete the tag to simulate non-existent tag
        tagRepository.delete(sourceTag);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source tag not found");
    }
    
    @Test
    void process_shouldHandleEmptyTracksList() throws IOException {
        // given
        // Create empty tracks response
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(TEST_DTO_ROOT);
        ObjectNode tracksNode = (ObjectNode) jsonNode.path("tracks");
        tracksNode.putArray("track"); // Replace tracks with empty array
        String emptyResponseBody = objectMapper.writeValueAsString(jsonNode);
        
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with empty tracks
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            emptyResponseBody, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);
        
        // Record initial state
        long initialTrackCount = trackRepository.count();
        long initialArtistCount = artistRepository.count();
        long initialRelationCount = artistTrackRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then - no new entities should be created
        assertEquals(initialTrackCount, trackRepository.count(),
            "No new tracks should be created for empty response");
        assertEquals(initialArtistCount, artistRepository.count(),
            "No new artists should be created for empty response");
        assertEquals(initialRelationCount, artistTrackRepository.count(),
            "No new relations should be created for empty response");
    }
    
    @Test
    void process_shouldHandleErrorGracefully_whenResponseIsInvalid() {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create invalid API response
        String invalidJson = "{\"invalid\": true}";
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            invalidJson, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);
        
        // when/then
        assertThrows(UnrecognizedPropertyException.class, () -> processor.processResponse(apiResponse),
            "Should throw exception when processing invalid response");
        
        // Verify no entities were created
        assertEquals(1, tagRepository.count(), "Only source tag should exist");
        assertEquals(0, trackRepository.count(), "No tracks should be created");
        assertEquals(0, artistRepository.count(), "No artists should be created");
    }
    
    @Test
    void process_shouldHandleArtistDeduplication() throws IOException {
        // given
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = createApiResponse(sourceTag);
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify Nirvana artist was created only once despite having two tracks
        List<LastfmArtist> nirvanaArtists = artistRepository.findAll().stream()
            .filter(artist -> artist.getName().equals("Nirvana"))
            .toList();
            
        assertEquals(1, nirvanaArtists.size(), 
            "Artist should be deduplicated when appearing multiple times");
            
        // Verify both Nirvana tracks are associated with the same artist
        LastfmArtist nirvanaArtist = nirvanaArtists.get(0);
        List<LastfmArtistTrack> nirvanaRelations = artistTrackRepository.findAll().stream()
            .filter(relation -> relation.getArtist().getId() == nirvanaArtist.getId())
            .toList();
            
        assertEquals(2, nirvanaRelations.size(), 
            "Both tracks should be associated with the same artist");
    }

    /**
     * Test dto root example contains three tracks, two of which belong to the same artist, to test deduplication
     */
    private final static String TEST_DTO_ROOT = """
        {
          "tracks": {
            "track": [
              {
                "name": "Smells Like Teen Spirit",
                "duration": "301",
                "mbid": "0ebe2d92-a11d-4b2b-9922-806383074ed7",
                "url": "https://www.last.fm/music/Nirvana/_/Smells+Like+Teen+Spirit",
                "streamable": {
                  "#text": "0",
                  "fulltrack": "0"
                },
                "artist": {
                  "name": "Nirvana",
                  "mbid": "9282c8b4-ca0b-4c6b-b7e3-4f7762dfc4d6",
                  "url": "https://www.last.fm/music/Nirvana"
                },
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  }
                ],
                "@attr": {
                  "rank": "1"
                }
              },
              {
                "name": "Creep",
                "duration": "239",
                "mbid": "d11fcceb-dfc5-4d19-b45d-f4e8f6d3eaa6",
                "url": "https://www.last.fm/music/Radiohead/_/Creep",
                "streamable": {
                  "#text": "0",
                  "fulltrack": "0"
                },
                "artist": {
                  "name": "Radiohead",
                  "mbid": "a74b1b7f-71a5-4011-9441-d0b5e4122711",
                  "url": "https://www.last.fm/music/Radiohead"
                },
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  }
                ],
                "@attr": {
                  "rank": "3"
                }
              },
              {
                "name": "Come as You Are",
                "duration": "208",
                "mbid": "e05035a3-14ac-4f88-a160-0a144530004e",
                "url": "https://www.last.fm/music/Nirvana/_/Come+as+You+Are",
                "streamable": {
                  "#text": "0",
                  "fulltrack": "0"
                },
                "artist": {
                  "name": "Nirvana",
                  "mbid": "9282c8b4-ca0b-4c6b-b7e3-4f7762dfc4d6",
                  "url": "https://www.last.fm/music/Nirvana"
                },
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  }
                ],
                "@attr": {
                  "rank": "4"
                }
              }
            ],
            "@attr": {
              "tag": "rock",
              "page": "1",
              "perPage": "50",
              "totalPages": "10385",
              "total": "519209"
            }
          }
        }
        """;
}
