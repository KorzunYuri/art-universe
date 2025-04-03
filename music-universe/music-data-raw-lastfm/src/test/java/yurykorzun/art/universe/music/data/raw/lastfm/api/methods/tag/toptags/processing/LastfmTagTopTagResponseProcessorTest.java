package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LastfmTagTopTagResponseProcessorTest extends FullContextTest {

    @Autowired
    private LastfmTagTopTagResponseProcessor processor;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    //  repositories, for which processor produces new entities
    @Autowired
    private LastfmTagRepository tagRepository;
    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @AfterEach
    public void cleanDatabase() {
        tagRepository.deleteAll();
        attributeHistoryRepository.deleteAll();

        consistencyHelper.cleanup();
    }

    private static final String TEST_DTO_STRING = """
            {
              "toptags": {
                "@attr": {
                  "offset": 0,
                  "num_res": 50,
                  "total": 2855332
                },
                "tag": [
                  {
                    "name": "rock",
                    "count": 4050770,
                    "reach": 401715
                  },
                  {
                    "name": "electronic",
                    "count": 2475740,
                    "reach": 260815
                  }
                ]
              }
            }
            """;
    private static final int TEST_DTO_TAGS_NUMBER = 2;
    private static final int SCD2_ATTRIBUTES_NUMBER = 2;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 1;

    private void testPrimarySave(LastfmApiResponse response, int tagsNumber) throws IOException {

        processor.processResponse(response);
        List<LastfmTag> createdTags = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> createdAttributeValues = attributeHistoryRepository.findAll();

        assertEquals(tagsNumber, createdTags.size(),
            "First save must save tags that haven't existed in DB");
        assertEquals(tagsNumber * (SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER), createdAttributeValues.size(),
            "First save must save (tags X (tag attributes)) attribute records");
    }

    @Test
    public void givenTagTopTagsResponse_whenProcessed_tagsAndAttributeValuesAreCreated() throws IOException {
        LastfmApiResponse response = consistencyHelper.createDummyApiResponse(TEST_DTO_STRING);

        testPrimarySave(response, TEST_DTO_TAGS_NUMBER);
    }

    @Test
    public void givenProcessedTagTopTagsResponse_whenProcessedTwice_tagsAndAttributeValuesAreNotDuplicated() throws IOException {
        LastfmApiResponse firstResponse = consistencyHelper.createDummyApiResponse(TEST_DTO_STRING);

        // initial save
        testPrimarySave(firstResponse, TEST_DTO_TAGS_NUMBER);

        // secondary save of the same data with the same snapshot
        processor.processResponse(firstResponse);
        List<LastfmTag> tagsAfterSecond = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterSecond = attributeHistoryRepository.findAll();

        assertEquals(TEST_DTO_TAGS_NUMBER, tagsAfterSecond.size(),
            "Second save of same entities must not produce duplicates");
        assertEquals(TEST_DTO_TAGS_NUMBER * (SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER), attributesAfterSecond.size(),
            "Second save of same attributes must not produce duplicates");
    }

    @Test
    public void givenProcessedTagTopTagsResponse_whenPartialUpdatesReceived_thenProducesOnlyNewRecords() throws IOException {
        LastfmApiResponse firstResponse = consistencyHelper.createDummyApiResponse(TEST_DTO_STRING);

        // initial save
        testPrimarySave(firstResponse, TEST_DTO_TAGS_NUMBER);

        // partially new save: new tag appears, old tag didn't change
        final int newTagsNumber = 1;
        final int retainedTagsNumber = 1;
        String newResponseBody = """
                {
                  "toptags": {
                    "@attr": {
                      "offset": 0,
                      "num_res": 50,
                      "total": 2855332
                    },
                    "tag": [
                      {
                        "name": "electronic",
                        "count": 2475740,
                        "reach": 260815
                      },
                      {
                        "name": "seen live",
                        "count": 2182394,
                        "reach": 82489
                      }
                    ]
                  }
                }
                """;
        LastfmApiResponse responseWithUpdates = consistencyHelper.createDummyApiResponse(newResponseBody);

        processor.processResponse(responseWithUpdates);
        List<LastfmTag> tagsAfterUpdate = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterUpdate = attributeHistoryRepository.findAll();

        int oldValuesNumber = TEST_DTO_TAGS_NUMBER * (SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER);
        int newValuesNumber = newTagsNumber * (SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER)
                            + retainedTagsNumber * SNAPSHOT_ATTRIBUTES_NUMBER;
        final int expectedValuesNumber = oldValuesNumber + newValuesNumber;

        assertEquals((TEST_DTO_TAGS_NUMBER + newTagsNumber), tagsAfterUpdate.size(),
            "Partially new batch of tags must produce only new tags");
        assertEquals(expectedValuesNumber, attributesAfterUpdate.size(),
            "Partially new batch of attributes must produce only new attribute values + snapshot values");
    }
}