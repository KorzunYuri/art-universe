package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.processing;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

@Tag("integration")
@Import({
    LastfmArtistSearchResponseProcessor.class,
    LastfmArtistSearchArtistFactory.class,
    LastfmApiDtoProcessingService.class,
})
class LastfmArtistSearchResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistSearchResponseProcessor processor;

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
    void givenArtistSearchApiResponseAndEmptyDb_whenProcessed_thenNewEntitiesCreated() throws IOException {
        String responseBody = LastfmApiClientResourceUtil.getApiClientResponse("artist.search");
        TestCase testCase = createTestCase("PUP", responseBody);

        ReflectionTestUtils.setField(processor, "artistSimilarityThreshold", 0.0);

        when(artistService.findAllByNames(any())).thenReturn(List.of());
        when(artistService.saveArtists(any())).thenAnswer(i -> i.getArgument(0));
        when(attributeHistoryService.upsertCandidateValues(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        final int expectedEntitiesNumber = 30;
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

        // verify entity relations are not saved
        verify(entityRelationService, never()).upsertEntityRelations(any());
    }

    @AllArgsConstructor
    private static class TestCase {
        LastfmApiResponse sourceApiResponse;
        List<LastfmArtist> expectedArtists;
    }

    private TestCase createTestCase(String searchString, String responseBody) {
        LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.ARTIST_SEARCH)
            .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, searchString))
        );
        LastfmApiResponse apiResponse = consistencyHelper.createAndSaveApiResponse(responseBody, sourceApiCall);
        EntityFactory<LastfmArtist, ArtistSearchArtistDto> entityFactory = new LastfmArtistSearchArtistFactory();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArtistSearchDtoRoot dtoRoot = objectMapper.readValue(responseBody, ArtistSearchDtoRoot.class);
            List<LastfmArtist> expectedArtists = dtoRoot.getRootObject().getMatches().getArtists().stream()
                .map(dto -> entityFactory.fromDto(dto, apiResponse))
                .toList();

            return new TestCase(apiResponse, expectedArtists);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}