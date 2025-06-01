package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

@Tag("integration")
@Import({
    LastfmTagTopArtistsResponseProcessor.class,
    LastfmTagTopArtistsArtistFactory.class,
    LastfmApiDtoProcessingService.class,
})
class LastfmTagTopArtistsResponseProcessorTest extends JpaOnlyTest {

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
    private LastfmArtistService artistService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int TEST_DTO_ENTITIES_NUMBER = 2;
    private static final int SCD2_ATTRIBUTES_NUMBER = 3;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 1;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenTagTopArtistsResponse_whenProcessed_newRecordsAreCreated() throws Exception {

        final int expectedCreatedArtistsNumber = TEST_DTO_ENTITIES_NUMBER;
        final int expectedCreatedAttrValuesNumber = expectedCreatedArtistsNumber * ATTRIBUTES_NUMBER;

        BaseLastfmEntity scopeEntity = EntityCreationHelper.createTag();
        LastfmApiResponse response = EntityCreationHelper.createApiResponse(
            TEST_DTO_ROOT, scopeEntity.getApiCall().getType(), scopeEntity);
        when(artistService.saveArtists(any())).thenAnswer(invocation -> invocation.getArgument(0));

        processor.processResponse(response);

        // Verify that artists were searched by names
        verifyAndAssertInvocations(
            captor -> verify(artistService).findAllByNames(captor.capture()),
            List.class,
            List.of(List.of("Coldplay", "Linkin Park")),
            "artistService.findAllByNameIn"
        );

        // Verify that new artists are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(artistService).saveArtists(captor.capture()),
            List.of(expectedCreatedArtistsNumber),
            "artistService.saveAll"
        );

        // Verify that entity relations were upserted
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(entityRelationService).upsertEntityRelations(captor.capture()),
            List.of(expectedCreatedArtistsNumber),
            "entityRelationService.upsertEntityRelations"
        );

        // Verify that attribute history records were upserted
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(expectedCreatedAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );
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