package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsRootDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;


class LastfmArtistTopTagsResponseProcessorTest extends FullContextTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopTagsResponseProcessor processor;

    // injections for verifications
    @MockitoBean
    private LastfmTagService tagService;
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int SCD2_ATTRIBUTES_NUMBER = 1;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 2;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @Test
    void givenArtistTopTagsResponse_whenProcessed_newRecordsAreCreated() throws IOException {

        // given
        String dtoResponseString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTags");
        TestCase testCase = testCaseFromResponse(dtoResponseString);

        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeHistoryService.upsertCandidateValues(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        final int expectedTagsNumber = 28;
        final int expectedAttrValuesNumber = ATTRIBUTES_NUMBER * expectedTagsNumber;

        // verify tags are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(tagService, times(1)).saveTags(captor.capture()),
            List.of(expectedTagsNumber),
            "artistService.findByName"
        );

        // verify attribute values are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService, times(1)).upsertCandidateValues(captor.capture()),
            List.of(expectedAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );

    }

    /**
     * Base test case for artist processor
     */
    @AllArgsConstructor
    private static class TestCase {
        LastfmApiResponse sourceApiResponse;
        LastfmArtist artist;
        List<LastfmTag> expectedTags;
    }

    private TestCase testCaseFromResponse(String responseString) {
        final ArtistTopTagsRootDto dtoRoot;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            dtoRoot = objectMapper.readValue(responseString, ArtistTopTagsRootDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LastfmArtist artist = consistencyHelper.createArtist(
            builder -> builder
                .name(dtoRoot.getTopTagsObject().getArtist().getName())
        );

        LastfmApiResponse sourceApiResponse = consistencyHelper.createDummyApiResponse(
            responseString, LastfmApiCallType.ARTIST_TOP_TAGS, artist);

        LastfmArtistTopTagsTagFactory tagFactory = new LastfmArtistTopTagsTagFactory();
        List<LastfmTag> expectedTags = dtoRoot.getTopTagsObject().getTags().stream()
            .map(dto -> tagFactory.fromDto(dto, sourceApiResponse))
            .toList();

        return new TestCase(sourceApiResponse, artist, expectedTags);
    }

}