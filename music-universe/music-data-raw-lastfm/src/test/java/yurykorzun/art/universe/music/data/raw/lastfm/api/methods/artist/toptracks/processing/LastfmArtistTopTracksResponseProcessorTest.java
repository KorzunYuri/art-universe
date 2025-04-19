package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.verifyAndAssertInvocations;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.verifyInvocationsNumberWithCollectionsSizeOnly;

class LastfmArtistTopTracksResponseProcessorTest extends FullContextTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopTracksResponseProcessor processor;

    // injections for verifications
    @MockitoBean
    private LastfmTrackService trackService;
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int TEST_DTO_ENTITIES_NUMBER = 50;
    private static final int SCD2_ATTRIBUTES_NUMBER = 5;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 0;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenTagTopArtistsResponse_whenProcessed_newRecordsAreCreated() throws Exception {

        // given
        final int expectedCreatedTracksNumber = TEST_DTO_ENTITIES_NUMBER;
        final int expectedCreatedAttrValuesNumber = expectedCreatedTracksNumber * ATTRIBUTES_NUMBER;

        ReflectionTestUtils.setField(processor, "trackListenersThreshold", 0); // isolate threshold effect

        TestCase testCase = testCaseFromResponse(LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTracks"));
        when(trackService.saveTracks(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // Verify that artists were searched by names
        verifyAndAssertInvocations(
            captor -> verify(trackService).findAllByUrls(captor.capture()),
            List.class,
            List.of(testCase.expectedTracks.stream().map(dto -> dto.getUrl()).toList()),
            "trackService.findAllByUrls"
        );

        // Verify that new artists are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(trackService).saveTracks(captor.capture()),
            List.of(expectedCreatedTracksNumber),
            "trackService.saveTracks"
        );

        // Verify that entity relations were upserted
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(entityRelationService).upsertEntityRelations(captor.capture()),
            List.of(expectedCreatedTracksNumber),
            "entityRelationService.upsertEntityRelations"
        );

        // Verify that attribute history records were upserted
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(expectedCreatedAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );
    }

    @AllArgsConstructor
    private static class TestCase {
        final LastfmApiResponse sourceApiResponse;
        final List<LastfmTrack> expectedTracks;
    }

    private TestCase testCaseFromResponse(String apiResponseBody) {
        BaseLastfmEntity scopeEntity = consistencyHelper.createDummyEntity(LastfmApiCallType.TAG_TOP_ARTISTS);
        LastfmApiResponse sourceApiResponse = consistencyHelper.createDummyApiResponse(
            apiResponseBody, scopeEntity.getApiCall().getType(), scopeEntity);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArtistTopTracksDtoRoot dtoRoot = objectMapper.readValue(apiResponseBody, ArtistTopTracksDtoRoot.class);

            LastfmTrackEntityFactory<ArtistTopTracksTrackDto> trackFactory = new LastfmArtistTopTracksTrackEntityFactory();
            List<LastfmTrack> expectedTracks = dtoRoot.getRootObject().getTracks().stream()
                .map(track -> trackFactory.fromDto(track, sourceApiResponse))
                .toList();

            return new TestCase(sourceApiResponse, expectedTracks);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}