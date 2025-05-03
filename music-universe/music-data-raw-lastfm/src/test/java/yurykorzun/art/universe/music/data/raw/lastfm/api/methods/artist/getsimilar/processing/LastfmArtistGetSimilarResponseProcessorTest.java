package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

@Import({
    LastfmArtistGetSimilarResponseProcessor.class,
    LastfmArtistGetSimilarArtistFactory.class,
    LastfmApiDtoProcessingService.class
})
class LastfmArtistGetSimilarResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistGetSimilarResponseProcessor processor;

    // injections for verifications
    @MockitoBean
    private LastfmArtistService artistService;
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int SCD2_ATTRIBUTES_NUMBER = 4;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 0;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @Test
    void givenArtistGetSimilarResponse_whenProcessed_newRecordsAreCreated() throws IOException {

        // given
        String dtoResponseString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getSimilar");
        TestCase testCase = testCaseFromResponse(dtoResponseString);

        when(artistService.saveArtists(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeHistoryService.upsertCandidateValues(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(processor, "artistMatchThreshold", (float) 0.0);

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        final int expectedEntitiesNumber = 50;
        final int expectedAttrValuesNumber = ATTRIBUTES_NUMBER * expectedEntitiesNumber;

        // verify that artists were searched by names
        verifyAndAssertInvocations(captor -> verify(artistService).findAllByNames(captor.capture()),
            List.class,
            List.of(testCase.expectedArtists.stream().map(artist -> artist.getName()).toList()),
            "artistService.findAllByNames"
        );

        // verify albums are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(artistService).saveArtists(captor.capture()),
            List.of(expectedEntitiesNumber),
            "artistService.saveArtists"
        );

        // verify attribute values are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(expectedAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );


    }
    @AllArgsConstructor
    private static class TestCase {
        LastfmApiResponse sourceApiResponse;
        LastfmArtist artist;
        List<LastfmArtist> expectedArtists;
    }

    private TestCase testCaseFromResponse(String responseString) {
        final ArtistGetSimilarDtoRoot dtoRoot;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            dtoRoot = objectMapper.readValue(responseString, ArtistGetSimilarDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LastfmArtist artist = consistencyHelper.createAndSaveArtist(
            builder -> builder.name("test artist")
        );

        LastfmApiResponse sourceApiResponse = consistencyHelper.createAndSaveApiResponse(
            responseString, LastfmApiCallType.ARTIST_GET_SIMILAR, artist);

        LastfmArtistGetSimilarArtistFactory entityFactory = new LastfmArtistGetSimilarArtistFactory();
        List<LastfmArtist> expectedArtists = dtoRoot.getRootObject().getArtists().stream()
            .map(dto -> entityFactory.fromDto(dto, sourceApiResponse))
            .toList();

        return new TestCase(sourceApiResponse, artist, expectedArtists);
    }
}