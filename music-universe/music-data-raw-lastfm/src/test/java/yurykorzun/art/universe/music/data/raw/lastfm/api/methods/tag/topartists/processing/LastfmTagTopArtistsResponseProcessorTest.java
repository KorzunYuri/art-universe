package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LastfmTagTopArtistsResponseProcessorTest extends FullContextTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopArtistsResponseProcessor processor;

    // injections for verifications
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;
    @MockitoBean
    private LastfmArtistRepository artistRepository;

    private static final int TEST_DTO_ENTITIES_NUMBER = 2;
    private static final int SCD2_ATTRIBUTES_NUMBER = 0;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 3;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void givenTagTopArtistsResponse_whenProcessed_newRecordsAreCreated() throws Exception {

        final int expectedCreatedArtistsNumber = TEST_DTO_ENTITIES_NUMBER;
        final int expectedCreatedAttrValuesNumber = expectedCreatedArtistsNumber * ATTRIBUTES_NUMBER;

        BaseLastfmEntity scopeEntity = consistencyHelper.createDummyEntity(LastfmApiCallType.TAG_TOP_ARTISTS);
        LastfmApiResponse response = consistencyHelper.createDummyApiResponse(
            TEST_DTO_ROOT, scopeEntity.getApiCall().getType(), scopeEntity);
        when(artistRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        processor.processResponse(response);

        // Verify that artistRepository.findAllByNameIn was called with the correct set of artist names.
        ArgumentCaptor<Collection<String>> namesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(artistRepository).findAllByNameIn(namesCaptor.capture());
        Collection<String> capturedNames = namesCaptor.getValue();
        assertEquals(expectedCreatedArtistsNumber, capturedNames.size(), "Expected 2 artist names to be searched");
        assertTrue(capturedNames.contains("Coldplay"), "Artist1 should be in the search list");
        assertTrue(capturedNames.contains("Linkin Park"), "Artist2 should be in the search list");

        // Verify that new artists are saved
        ArgumentCaptor<List<LastfmArtist>> artistCaptor = ArgumentCaptor.forClass(List.class);
        verify(artistRepository).saveAll(artistCaptor.capture());
        List<LastfmArtist> savedArtists = artistCaptor.getValue();
        assertEquals(expectedCreatedArtistsNumber, savedArtists.size(), "Expected 2 new artists to be saved");

        // Verify that entity relations are created.
        ArgumentCaptor<List<LastfmEntityRelation>> relCaptor = ArgumentCaptor.forClass(List.class);
        verify(entityRelationService).upsertEntityRelations(relCaptor.capture());
        List<LastfmEntityRelation> relations = relCaptor.getValue();
        assertEquals(expectedCreatedArtistsNumber, relations.size(), "Expected 2 entity relations to be created");

        // Verify that attribute history records are upserted.
        ArgumentCaptor<List<LastfmAttributeHistoryRecord>> attrCaptor = ArgumentCaptor.forClass(List.class);
        verify(attributeHistoryService).upsertCandidateValues(attrCaptor.capture());
        List<LastfmAttributeHistoryRecord> attrValues = attrCaptor.getValue();
        assertEquals(expectedCreatedAttrValuesNumber, attrValues.size(),
            String.format("Expected %d attribute history records to be created", expectedCreatedAttrValuesNumber));
    }

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