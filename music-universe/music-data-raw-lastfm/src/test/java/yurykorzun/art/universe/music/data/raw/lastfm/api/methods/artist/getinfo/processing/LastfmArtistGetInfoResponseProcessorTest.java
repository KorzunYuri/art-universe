package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

@Import({
    LastfmArtistGetInfoResponseProcessor.class,
    LastfmArtistGetInfoArtistFactory.class,
    LastfmArtistGetInfoSimilarArtistFactory.class,
    LastfmArtistGetInfoTagFactory.class,
    LastfmApiDtoProcessingService.class,
})
class LastfmArtistGetInfoResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistGetInfoResponseProcessor processor;

    // beans for invocations verification
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;
    @MockitoBean
    private LastfmArtistService artistService;
    @MockitoBean
    private LastfmTagService tagService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int ARTIST_SCD2_ATTRS_NUMBER = 6;
    private static final int ARTIST_SNAPSHOT_ATTRS_NUMBER = 0;
    private static final int ARTIST_ATTRS_NUMBER = ARTIST_SCD2_ATTRS_NUMBER + ARTIST_SNAPSHOT_ATTRS_NUMBER;
    private static final int TAG_SCD2_ATTRS_NUMBER = 1;
    private static final int TAG_SNAPSHOT_ATTRS_NUMBER = 0;
    private static final int TAG_ATTRS_NUMBER = TAG_SCD2_ATTRS_NUMBER + TAG_SNAPSHOT_ATTRS_NUMBER;
    private static final int SIMILAR_ARTIST_SCD2_ATTRS_NUMBER = 1;
    private static final int SIMILAR_ARTIST_SNAPSHOT_ATTRS_NUMBER = 0;
    private static final int SIMILAR_ARTIST_ATTRS_NUMBER = SIMILAR_ARTIST_SCD2_ATTRS_NUMBER + SIMILAR_ARTIST_SNAPSHOT_ATTRS_NUMBER;

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenExistingApprovedArtist_whenProcessedArtistGetInfoResponse_newRecordsAreCreated() throws Exception {

        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getInfo");
        TestCase testCase = testCaseFromResponse(responseJsonString, true);

        // expected values
        final int newArtistAttrValuesNumber = ARTIST_ATTRS_NUMBER;
        final int newTagsNumber = testCase.expectedTags.size();
        final int newTagAttrValuesNumber = newTagsNumber * TAG_ATTRS_NUMBER;

        when(artistService.findByName(eq(testCase.expectedArtist.getName()))).thenReturn(Optional.of(testCase.expectedArtist));
        when(artistService.saveArtists(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(attributeHistoryService.upsertCandidateValue(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        processor.processResponse(testCase.sourceApiResponse);

        // Verify that similar artists were searched
        verify(artistService, never()).findAllByNames(any());

        // Verify that base artist and similar artists were saved
        verifyAndAssertInvocations(
            captor -> verify(artistService, times(1)).saveArtists(captor.capture()),
            List.class,
            List.of(List.of(testCase.expectedArtist)),
            "artistService.saveArtists");

        // Verify that new tags were saved
        verifyAndAssertInvocations(
            captor -> verify(tagService, times(1)).saveTags(captor.capture()),
            List.class,
            List.of(testCase.expectedTags),
            "tagService.saveTags");

        // verify that attribute values were saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService, times(2)).upsertCandidateValues(captor.capture()),
            List.of(newArtistAttrValuesNumber, newTagAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues");

        // verify that entity relations were saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(entityRelationService, times(1)).upsertEntityRelations(captor.capture()),
            List.of(newTagsNumber),
            "entityRelationService.upsertEntityRelations");

    }

    @Test
    @SuppressWarnings("unchecked")
    void givenExistingUnapprovedArtist_whenProcessedArtistGetInfoResponse_newRecordsAreCreated() throws Exception {

        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getInfo");
        TestCase testCase = testCaseFromResponse(responseJsonString, false);

        // expected values
        final int newArtistAttrValuesNumber = ARTIST_ATTRS_NUMBER;
        final int newTagsNumber = testCase.expectedTags.size();
        final int newTagAttrValuesNumber = newTagsNumber * TAG_ATTRS_NUMBER;

        when(artistService.findByName(eq(testCase.expectedArtist.getName()))).thenReturn(Optional.of(testCase.expectedArtist));
        when(artistService.saveArtists(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(attributeHistoryService.upsertCandidateValue(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        processor.processResponse(testCase.sourceApiResponse);

        // Verify that similar artists were not searched, because base artist is not approved
        verify(artistService, never()).findAllByNames(any());

        // Verify that base artist and similar artists were saved
        verifyAndAssertInvocations(
            captor -> verify(artistService, times(1)).saveArtists(captor.capture()),
            List.class,
            List.of(List.of(testCase.expectedArtist)),
            "artistService.saveArtists");

        // Verify that new tags were saved
        verifyAndAssertInvocations(
            captor -> verify(tagService, times(1)).saveTags(captor.capture()),
            List.class,
            List.of(testCase.expectedTags),
            "tagService.saveTags");

        // verify that attribute values were saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService, times(2)).upsertCandidateValues(captor.capture()),
            List.of(newArtistAttrValuesNumber, newTagAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues");

        // verify that entity relations were saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(entityRelationService, times(1)).upsertEntityRelations(captor.capture()),
            List.of(newTagsNumber),
            "entityRelationService.upsertEntityRelations");

    }

    /**
     * Base test case for artist processor
     */
    @AllArgsConstructor
    private static class TestCase {
        LastfmApiResponse sourceApiResponse;
        LastfmArtist expectedArtist;
        List<LastfmTag> expectedTags;
    }

    private TestCase testCaseFromResponse(String responseString, boolean isApproved) {

        final ArtistGetInfoDtoRoot dtoRoot;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            dtoRoot = objectMapper.readValue(responseString, ArtistGetInfoDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LastfmArtist expectedArtist = consistencyHelper.createAndSaveArtist(
            builder -> builder
                .name(dtoRoot.getArtist().getName()) // for processor to find the record
                .approvalStatus(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING) // affect processor's logic
        );

        LastfmApiResponse sourceApiResponse = consistencyHelper.createDummyApiResponse(
            responseString, LastfmApiCallType.ARTIST_GET_INFO, expectedArtist);

        LastfmTagEntityFactory<ArtistGetInfoArtistTagDto> tagFactory = new LastfmTagEntityFactory<>();
        List<LastfmTag> expectedTags = dtoRoot.getArtist().getTagsObject().getTags().stream()
            .map(dto -> tagFactory.fromDto(dto, sourceApiResponse))
            .toList();

        return new TestCase(sourceApiResponse, expectedArtist, expectedTags);
    }

}