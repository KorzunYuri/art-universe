package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LastfmArtistGetInfoResponseProcessorTest extends FullContextTest {

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
    void givenExistingArtist_whenProcessedArtistGetInfoResponse_newRecordsAreCreated() throws Exception {

        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getInfo");
        TestCase testCase = testCaseFromResponse(responseJsonString);

        // expected values
        final int newArtistAttrValuesNumber = ARTIST_ATTRS_NUMBER;
        final int newSimilarArtistsNumber = testCase.expectedSimilarArtists.size();
        final int newSimilarArtistsAttrValuesNumber = newSimilarArtistsNumber * SIMILAR_ARTIST_ATTRS_NUMBER;
        final int newTagsNumber = testCase.expectedTags.size();
        final int newTagAttrValuesNumber = newTagsNumber * TAG_ATTRS_NUMBER;

        when(artistService.saveArtists(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(tagService.saveTags(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(attributeHistoryService.upsertCandidateValue(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        processor.processResponse(testCase.sourceApiResponse);

        verify(artistService, times(1)).findAllByNames(any());

        ArgumentCaptor<List<String>> namesCaptor = ArgumentCaptor.forClass(List.class);
        verify(artistService, times(1)).findAllByNames(namesCaptor.capture());
        List<String> capturedNames = namesCaptor.getValue();
        assertEquals(newSimilarArtistsNumber, capturedNames.size(),
            String.format("Expected %d artist names to be searched", newSimilarArtistsNumber));

        // Verify that new artists were saved
        ArgumentCaptor<List<LastfmArtist>> artistsCaptor = ArgumentCaptor.forClass(List.class);
        verify(artistService, times(2)).saveArtists(artistsCaptor.capture());
        List<List<LastfmArtist>> savedArtistsInvocationParams = artistsCaptor.getAllValues();
        List<LastfmArtist> saveBaseArtist = savedArtistsInvocationParams.get(0);
        assertEquals(1, saveBaseArtist.size(), "Expected base artist to be save first");
        assertEquals(testCase.expectedArtist, saveBaseArtist.get(0));
        List<LastfmArtist> savedSimilarArtists = savedArtistsInvocationParams.get(1);
        assertEquals(newSimilarArtistsNumber, savedSimilarArtists.size(),
            String.format("Expected %s similar artists to be saved", newSimilarArtistsNumber));
        assertThat(testCase.expectedSimilarArtists, Matchers.containsInAnyOrder(savedSimilarArtists.toArray()));

        // Verity that new tags were saved
        ArgumentCaptor<List<LastfmTag>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(tagService, times(1)).saveTags(tagsCaptor.capture());
        List<LastfmTag> savedTags = tagsCaptor.getValue();
        assertEquals(newTagsNumber, savedTags.size());
        assertThat(testCase.expectedTags, Matchers.containsInAnyOrder(savedTags.toArray()));

        // Verify that attributes were saved as expected
        ArgumentCaptor<List<LastfmAttributeHistoryRecord>> attrValuesCaptor = ArgumentCaptor.forClass(List.class);
        verify(attributeHistoryService, times(3)).upsertCandidateValues(attrValuesCaptor.capture());
        List<List<LastfmAttributeHistoryRecord>> saveAttrValuesInvocationParams = attrValuesCaptor.getAllValues();
        List<LastfmAttributeHistoryRecord> artistAttrs = saveAttrValuesInvocationParams.get(0);
        assertEquals(newArtistAttrValuesNumber, artistAttrs.size());
        List<LastfmAttributeHistoryRecord> similarArtistsAttrs = saveAttrValuesInvocationParams.get(1);
        assertEquals(newSimilarArtistsAttrValuesNumber, similarArtistsAttrs.size());
        List<LastfmAttributeHistoryRecord> tagAttrs = saveAttrValuesInvocationParams.get(2);
        assertEquals(newTagAttrValuesNumber, tagAttrs.size());

        // verify that entity relations were saved as expected
        ArgumentCaptor<List<LastfmEntityRelation>> relCaptor = ArgumentCaptor.forClass(List.class);
        verify(entityRelationService, times(2)).upsertEntityRelations(relCaptor.capture());
        List<List<LastfmEntityRelation>> saveRelationsInvocationParams = relCaptor.getAllValues();
        List<LastfmEntityRelation> artistArtistRelations = saveRelationsInvocationParams.get(0);
        assertEquals(newSimilarArtistsNumber, artistArtistRelations.size());
        List<LastfmEntityRelation> tagArtistRelations = saveRelationsInvocationParams.get(1);
        assertEquals(newTagsNumber, tagArtistRelations.size());
    }

    @AllArgsConstructor
    private static class TestCase {
        final LastfmApiCall sourceApiCall;
        final LastfmApiResponse sourceApiResponse;
        final LastfmArtist expectedArtist;
        final List<LastfmArtist> expectedSimilarArtists;
        final List<LastfmTag> expectedTags;
    }

    private TestCase testCaseFromResponse(String responseString) {

        LastfmArtist scopeEntity = consistencyHelper.createAndSaveArtist(LastfmApiCallType.TAG_TOP_ARTISTS);
        LastfmApiResponse sourceApiResponse = consistencyHelper.createDummyApiResponse(
            responseString, LastfmApiCallType.ARTIST_GET_INFO, scopeEntity);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArtistGetInfoDtoRoot dtoRoot = objectMapper.readValue(responseString, ArtistGetInfoDtoRoot.class);

            LastfmArtistEntityFactory<ArtistGetInfoArtistDto> artistFactory = new LastfmArtistGetInfoArtistFactory();
            LastfmArtist expectedArtist = artistFactory.fromDto(dtoRoot.getArtist(), sourceApiResponse);

            LastfmArtistEntityFactory<ArtistDto> similarArtistFactory = new LastfmArtistEntityFactory<>();
            List<LastfmArtist> expectedSimilarArtists = dtoRoot.getArtist().getSimilarArtistsObject().getArtists().stream()
                .map(dto -> similarArtistFactory.fromDto(dto, sourceApiResponse))
                .toList();

            LastfmTagEntityFactory<ArtistGetInfoArtistTagDto> tagFactory = new LastfmTagEntityFactory<>();
            List<LastfmTag> expectedTags = dtoRoot.getArtist().getTagsObject().getTags().stream()
                .map(dto -> tagFactory.fromDto(dto, sourceApiResponse))
                .toList();

            return new TestCase(sourceApiResponse.getApiCall(), sourceApiResponse, expectedArtist, expectedSimilarArtists, expectedTags);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}