package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

@Tag("integration")
@Import({
    LastfmArtistTopAlbumsResponseProcessor.class,
    LastfmArtistTopAlbumsAlbumFactory.class,
    LastfmApiDtoProcessingService.class,
})
class LastfmArtistTopAlbumsResponseProcessorTest extends JpaOnlyTest {

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopAlbumsResponseProcessor processor;

    // injections for verifications
    @MockitoBean
    private LastfmAlbumService albumService;
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int SCD2_ATTRIBUTES_NUMBER = 3;
    private static final int SNAPSHOT_ATTRIBUTES_NUMBER = 0;
    private static final int ATTRIBUTES_NUMBER = SCD2_ATTRIBUTES_NUMBER + SNAPSHOT_ATTRIBUTES_NUMBER;

    @Test
    void process_shouldCreateNewRecords_whenArtistTopAlbumsResponseProvided() throws IOException {

        // given
        String dtoResponseString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopAlbums");
        TestCase testCase = testCaseFromResponse(dtoResponseString);

        ReflectionTestUtils.setField(processor, "albumPlayCountThreshold", 0); // isolate threshold effect

        when(albumService.saveAlbums(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeHistoryService.upsertCandidateValues(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        processor.processResponse(testCase.sourceApiResponse);

        // then
        final int expectedAlbumsNumber = 50;
        final int expectedAttrValuesNumber = ATTRIBUTES_NUMBER * expectedAlbumsNumber;

        // verify albums are searched by urls
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(albumService).findAllByUrls(captor.capture()),
            List.of(expectedAlbumsNumber),
            "albumService.findAllByUrls"
        );

        // verify albums are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(albumService).saveAlbums(captor.capture()),
            List.of(expectedAlbumsNumber),
            "albumService.saveAlbums"
        );

        // verify attribute values are saved
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService).upsertCandidateValues(captor.capture()),
            List.of(expectedAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );

        // verify entity relations are saved
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
        List<LastfmAlbum> expectedAlbums;
    }

    private TestCase testCaseFromResponse(String responseString) {
        final ArtistTopAlbumsDtoRoot dtoRoot;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            dtoRoot = objectMapper.readValue(responseString, ArtistTopAlbumsDtoRoot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LastfmArtist artist = consistencyHelper.createAndSaveArtist(
            builder -> builder.name("test artist")
        );

        LastfmApiResponse sourceApiResponse = consistencyHelper.createAndSaveApiResponse(
            responseString, LastfmApiCallType.ARTIST_TOP_ALBUMS, artist);

        LastfmArtistTopAlbumsAlbumFactory entityFactory = new LastfmArtistTopAlbumsAlbumFactory();
        List<LastfmAlbum> expectedAlbums = dtoRoot.getTopAlbumsObject().getAlbums().stream()
            .map(dto -> entityFactory.fromDto(dto, sourceApiResponse))
            .toList();

        return new TestCase(sourceApiResponse, artist, expectedAlbums);
    }

}