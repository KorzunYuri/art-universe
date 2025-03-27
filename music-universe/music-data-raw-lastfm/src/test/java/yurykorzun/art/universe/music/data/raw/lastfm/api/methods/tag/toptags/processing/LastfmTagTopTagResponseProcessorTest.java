package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LastfmTagTopTagResponseProcessorTest extends FullContextTest {

    @Autowired
    private LastfmTagTopTagResponseProcessor processor;

    //  repositories, for which processor produces new entities

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    // injections for creating consistent state in db

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @AfterEach
    public void reset() {
        cleanDatabase();
    }

    @AfterEach
    public void cleanDatabase() {
        tagRepository.deleteAll();
        attributeHistoryRepository.deleteAll();
    }

    private static final String TEST_DTO = """
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

    @Test
    public void givenTagTopTagsResponse_whenProcessed_tagsAndAttributeValuesAreCreated() throws IOException {
        LastfmApiResponse response = createApiResponse(TEST_DTO);
        final int tagsNumber = 2;
        final int attributesNumber = 3;

        // initial save
        processor.processResponse(response);
        List<LastfmTag> createdTags = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> createdAttributeValues = attributeHistoryRepository.findAll();

        assertEquals(tagsNumber, createdTags.size(),
                "First save must save tags that haven't existed in DB");
        assertEquals(tagsNumber * attributesNumber, createdAttributeValues.size(),
                "First save must save (tags X (tag attributes)) attribute records");
    }

    @Test
    public void givenTagTopTagsResponse_whenProcessedTwice_tagsAndAttributeValuesAreNotDuplicated() throws IOException {
        LastfmApiResponse response = createApiResponse(TEST_DTO);
        final int tagsNumber = 2;
        final int attributesNumber = 3;

        // initial save
        processor.processResponse(response);
        List<LastfmTag> tagsAfterFirst = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterFirst = attributeHistoryRepository.findAll();

        assertEquals(tagsNumber, tagsAfterFirst.size(),
                "First save must save tags that haven't existed in DB");
        assertEquals(tagsNumber * attributesNumber, attributesAfterFirst.size(),
                "First save must save (tags X (tag attributes)) attribute records");

        // secondary save
        processor.processResponse(response);
        List<LastfmTag> tagsAfterSecond = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterSecond = attributeHistoryRepository.findAll();

        assertEquals(tagsAfterFirst.size(), tagsAfterSecond.size(), "Second save must not produce duplicate tags");
        assertEquals(tagsNumber * attributesNumber * 2, attributesAfterSecond.size(),
                "Second save must produce new tag attribute values (tags X (tag attributes)) records");

        // partially new save
        // ONE new tag appears
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
        LastfmApiResponse newResponse = createApiResponse(newResponseBody);

        processor.processResponse(newResponse);
        List<LastfmTag> tagsAfterThird = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterThird = attributeHistoryRepository.findAll();

        assertEquals(tagsAfterFirst.size() + 1, tagsAfterThird.size(), "Second save must not produce duplicate tags");
        assertEquals(tagsNumber * attributesNumber * 3, attributesAfterThird.size(),
                "Second save must produce new tag attribute values (tags X (tag attributes)) records");
    }

    private LastfmApiResponse createApiResponse(String responseBody) {
        LastfmApiCall apiCall = createDummyApiCall();
        return LastfmApiResponse.builder()
                .responseBody(responseBody)
                .apiCall(apiCall)
            .build();
    }

    private LastfmDataSnapshot createDummyDataSnapshot() {
        return snapshotRepository.save(new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, new Date()));
    }

    private LastfmApiCall createDummyApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot();
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
                .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }
}