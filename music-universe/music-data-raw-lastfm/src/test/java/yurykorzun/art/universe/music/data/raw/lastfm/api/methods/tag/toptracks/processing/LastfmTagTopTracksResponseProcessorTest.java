package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
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
    LastfmTagTopTracksResponseProcessor.class,
    LastfmTagTopTracksArtistFactory.class,
    LastfmTagTopTracksTrackFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmArtistServiceImpl.class,
    LastfmTrackServiceImpl.class,
    LastfmTagServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmArtistTrackServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
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

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopTracksResponseProvided() throws IOException {
        // given
        String responseBody = TEST_DTO_ROOT;
        TagTopTracksDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);
        
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
        
        // Verify track properties
        List<LastfmTrack> savedTracks = trackRepository.findAll();
        for (LastfmTrack track : savedTracks) {
            assertNotNull(track.getName(), "Track name should be set");
            assertNotNull(track.getUrl(), "Track URL should be set");
            assertNotNull(track.getDuration(), "Track duration should be set");
        }
        
        // Verify artist properties
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            assertNotNull(artist.getUrl(), "Artist URL should be set");
        }
        
        // Verify relation properties
        List<LastfmArtistTrack> relations = artistTrackRepository.findAll();
        for (LastfmArtistTrack relation : relations) {
            assertNotNull(relation.getArtist(), "Relation should reference an artist");
            assertNotNull(relation.getTrack(), "Relation should reference a track");
        }
    }

    @Test
    void process_shouldThrowException_whenSourceTagNotFound() throws IOException {
        // given
        String responseBody = TEST_DTO_ROOT;
        
        // Create source tag first (needed for API call creation)
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response with the tag
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.TAG_TOP_TRACKS, sourceTag);
        
        // Now delete the tag to simulate non-existent tag
        tagRepository.delete(sourceTag);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source tag not found");
    }

    private TagTopTracksDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, TagTopTracksDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
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
