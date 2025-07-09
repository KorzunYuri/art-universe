package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl.LastfmArtistServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl.LastfmAttributeHistoryServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository.LastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    LastfmTagTopArtistsResponseProcessor.class,
    LastfmTagTopArtistsArtistFactory.class,
    LastfmApiDtoProcessingService.class,
    LastfmArtistServiceImpl.class,
    LastfmTagServiceImpl.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmArtistTagServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class
})
class LastfmTagTopArtistsResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopArtistsResponseProcessor processor;

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @Autowired
    private LastfmArtistTagRepository artistTagRepository;

    @BeforeEach
    public void setUp() {
        consistencyHelper.cleanup();
    }

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void process_shouldCreateNewRecords_whenTagTopArtistsResponseProvided() throws IOException {
        // given
        String responseBody = TEST_DTO_ROOT;
        TagTopArtistsDtoRoot dtoRoot = parseResponse(responseBody);
        
        // Create source tag
        LastfmTag sourceTag = consistencyHelper.createAndSaveTag();
        
        // Create API response
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(
            responseBody, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);
        
        // Record initial state
        long initialArtistCount = artistRepository.count();
        long initialAttributeCount = attributeHistoryRepository.count();
        long initialArtistTagCount = artistTagRepository.count();
        
        // when
        processor.processResponse(apiResponse);
        
        // then
        // Verify new artists were created
        int expectedArtistsCount = dtoRoot.getTopArtists().getArtists().size();
        assertEquals(initialArtistCount + expectedArtistsCount, artistRepository.count(), 
            "New artists should be created");
        
        // Verify attribute history records were created
        assertTrue(attributeHistoryRepository.count() > initialAttributeCount, 
            "New attribute history records should be created");
        
        // Verify artist-tag relations were created
        assertEquals(initialArtistTagCount + expectedArtistsCount, artistTagRepository.count(), 
            "Artist-tag relations should be created");
        
        // Verify artist properties
        List<LastfmArtist> savedArtists = artistRepository.findAll();
        for (LastfmArtist artist : savedArtists) {
            assertNotNull(artist.getName(), "Artist name should be set");
            assertNotNull(artist.getUrl(), "Artist URL should be set");
        }
        
        // Verify relation properties
        List<LastfmArtistTag> relations = artistTagRepository.findAll();
        for (LastfmArtistTag relation : relations) {
            assertEquals(sourceTag.getId(), relation.getTag().getId(), 
                "Relation should reference the source tag");
            assertNotNull(relation.getArtist(), "Relation should reference an artist");
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
            responseBody, LastfmApiCallType.TAG_TOP_ARTISTS, sourceTag);
        
        // Now delete the tag to simulate non-existent tag
        tagRepository.delete(sourceTag);
        
        // when/then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            processor.processResponse(apiResponse);
        }, "Should throw EntityNotFoundException when source tag not found");
    }

    private TagTopArtistsDtoRoot parseResponse(String responseString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseString, TagTopArtistsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    // "image" array is ignored but is left here to make it realistic
    private static final String TEST_DTO_ROOT = """
        {
          "topartists": {
            "artist": [
              {
                "name": "Coldplay",
                "mbid": "cc197bad-dc9c-440d-a5b5-d52ba2e14234",
                "url": "https://www.last.fm/music/Coldplay",
                "streamable": "0",
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
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "mega"
                  }
                ],
                "@attr": {
                  "rank": "1"
                }
              },
              {
                "name": "Linkin Park",
                "mbid": "f59c5520-5f46-4d2c-b2c4-822eabf53419",
                "url": "https://www.last.fm/music/Linkin+Park",
                "streamable": "0",
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
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "mega"
                  }
                ],
                "@attr": {
                  "rank": "2"
                }
              }
            ],
            "@attr": {
              "tag": "rock",
              "page": "1",
              "perPage": "50",
              "totalPages": "3578",
              "total": "178853"
            }
          }
        }
        """;
}
