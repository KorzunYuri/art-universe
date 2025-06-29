package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

@Tag("integration")
@Import({
    LastfmTagTopTagResponseProcessor.class,
    LastfmTagTopTagsTagFactory.class,
    LastfmApiDtoProcessingService.class,
})
class LastfmTagTopTagResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmTagTopTagResponseProcessor processor;

    // injections for verifications
    @MockitoBean
    private LastfmTagService tagService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int SCD2_ATTRIBUTES_NUMBER = 2;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 1;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @Test
    void givenTagTopTagsResponse_whenProcessed_newRecordsAreCreated() throws IOException {

        // given
        String dtoResponseString = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopTags");
        TestCase testCase = testCaseFromResponse(dtoResponseString);

        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeHistoryService.upsertCandidateValues(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        final int expectedEntitiesNumber = 50;
        final int expectedAttrValuesNumber = ATTRIBUTES_NUMBER * expectedEntitiesNumber;

        // verify that tags were searched by names
        verifyAndAssertInvocations(captor -> verify(tagService).findAllByNameIn(captor.capture()),
            List.class,
            List.of(testCase.expectedTags.stream().map(LastfmTag::getName).toList()),
            "tagService.findAllByNameIn"
        );

        // verify tags are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(tagService).saveTags(captor.capture()),
            List.of(expectedEntitiesNumber),
            "tagService.saveTags"
        );

        // verify attribute values are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(expectedAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );
    }

    @Test
    void givenTagTopTagsResponse_whenProcessedWithExistingTags_thenAllTagsAreSaved() throws IOException {

        // given
        String dtoResponseString = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopTags");
        TestCase testCase = testCaseFromResponse(dtoResponseString);

        // Mock existing tags (first 10 tags already exist)
        List<LastfmTag> existingTags = testCase.expectedTags.subList(0, 10);
        when(tagService.findAllByNameIn(any())).thenReturn(existingTags);
        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeHistoryService.upsertCandidateValues(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        // All tags are saved because existing tags also get updated attributes
        final int expectedSavedEntitiesNumber = 50; // All tags (new + existing with updated attributes)
        final int expectedTotalAttrValuesNumber = ATTRIBUTES_NUMBER * 50; // All tags get attribute values

        // verify that tags were searched by names
        verifyAndAssertInvocations(captor -> verify(tagService).findAllByNameIn(captor.capture()),
            List.class,
            List.of(testCase.expectedTags.stream().map(LastfmTag::getName).toList()),
            "tagService.findAllByNameIn"
        );

        // verify all tags are saved (new + existing with updated attributes)
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(tagService).saveTags(captor.capture()),
            List.of(expectedSavedEntitiesNumber),
            "tagService.saveTags"
        );

        // verify attribute values are saved for all tags
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(expectedTotalAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );
    }

    @Test
    void givenEmptyTagTopTagsResponse_whenProcessed_thenNoRecordsAreCreated() throws IOException {

        // given
        String emptyResponseString = """
            {
              "toptags": {
                "@attr": {
                  "offset": 0,
                  "num_res": 0,
                  "total": 0
                },
                "tag": []
              }
            }
            """;
        TestCase testCase = testCaseFromResponse(emptyResponseString);

        when(tagService.findAllByNameIn(any())).thenReturn(List.of());
        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        // verify that tags were searched by names (empty list)
        verifyAndAssertInvocations(captor -> verify(tagService).findAllByNameIn(captor.capture()),
            List.class,
            List.of(List.of()),
            "tagService.findAllByNameIn"
        );

        // verify saveTags is called with empty list
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(tagService).saveTags(captor.capture()),
            List.of(0),
            "tagService.saveTags"
        );

        // verify attribute values are saved (empty list)
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(0),
            "attributeHistoryService.upsertCandidateValues"
        );
    }

    @AllArgsConstructor
    private static class TestCase {
        LastfmApiResponse sourceApiResponse;
        List<LastfmTag> expectedTags;
    }

    private TestCase testCaseFromResponse(String responseString) {
        final TagTopTagsDtoRoot dtoRoot;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            dtoRoot = objectMapper.readValue(responseString, TagTopTagsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LastfmApiResponse sourceApiResponse = consistencyHelper.createAndSaveApiResponse(
            responseString, LastfmApiCallType.TAG_TOP_TAGS);

        LastfmTagTopTagsTagFactory entityFactory = new LastfmTagTopTagsTagFactory();
        List<LastfmTag> expectedTags = dtoRoot.getTopTags().getTags().stream()
            .map(dto -> entityFactory.fromDto(dto, sourceApiResponse))
            .toList();

        return new TestCase(sourceApiResponse, expectedTags);
    }
}
