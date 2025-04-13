package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

class LastfmTagTopTracksResponseProcessorTest extends FullContextTest {
    
    @Autowired
    private DbConsistencyHelper consistencyHelper;
    
    @Autowired
    private LastfmTagTopTracksResponseProcessor processor;

    // beans for invocations verification
    @MockitoBean
    private LastfmEntityRelationService entityRelationService;
    @MockitoBean
    private LastfmAttributeHistoryService attributeHistoryService;
    @MockitoBean
    private LastfmArtistService artistService;
    @MockitoBean
    private LastfmTrackService trackService;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int TRACK_SCD2_ATTRS_NUMBER = 4;
    private static final int TRACK_SNAPSHOT_ATTRS_NUMBER = 0;
    private static final int TRACK_ATTRS_NUMBER = TRACK_SCD2_ATTRS_NUMBER + TRACK_SNAPSHOT_ATTRS_NUMBER;
    private static final int ARTIST_SCD2_ATTRS_NUMBER = 2;
    private static final int ARTIST_SNAPSHOT_ATTRS_NUMBER = 0;
    private static final int ARTIST_ATTRS_NUMBER = ARTIST_SCD2_ATTRS_NUMBER + ARTIST_SNAPSHOT_ATTRS_NUMBER;

    @AfterEach
    public void cleanDatabase() {
        consistencyHelper.cleanup();
    }

    @Test
    void givenEmptyDatabase_whenProcessedTagTopTracksResponse_newRecordsAreCreated() throws Exception {

        TestCase testCase = testCaseFromResponse(TEST_DTO_ROOT);

        final int expectedCreatedTracksNumber = testCase.expectedTracks.size();
        final int expectedCreatedArtistsNumber = testCase.expectedArtists.size();
        final int expectedCreatedTrackAttrValuesNumber = expectedCreatedTracksNumber * TRACK_ATTRS_NUMBER;
        final int expectedCreatedArtistAttrValuesNumber = expectedCreatedArtistsNumber * ARTIST_ATTRS_NUMBER;

        when(trackService.saveTracks(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(artistService.saveArtists(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(attributeHistoryService.upsertCandidateValue(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        processor.processResponse(testCase.sourceApiResponse);

        // Verify that tracks were searched by urls
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(trackService, times(1)).findAllByUrls(captor.capture()),
            List.of(expectedCreatedTracksNumber),
            "trackService.findAllByUrls"
        );

        // Verify that new tracks were saved
        verifyAndAssertInvocations(
            captor -> verify(trackService, times(1)).saveTracks(captor.capture()),
            List.class,
            List.of(testCase.expectedTracks),
            "trackService.saveTracks"
        );

        // Verify that artists were searched by names.
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(artistService, times(1)).findAllByNames(captor.capture()),
            List.of(expectedCreatedArtistsNumber),
            "artistService.findAllByNames"
        );

        // Verify that new artists are saved
        verifyAndAssertInvocations(
            captor -> verify(artistService, times(1)).saveArtists(captor.capture()),
            List.class,
            List.of(testCase.expectedArtists),
            "artistService.saveArtists"
        );

        // Verify that entity relations were created
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(entityRelationService, times(2)).upsertEntityRelations(captor.capture()),
            List.of(expectedCreatedTracksNumber, expectedCreatedTracksNumber),
            "entityRelationService.upsertEntityRelations"
        );

        // Verify that attribute values were upserted
        verifyInvocationsNumberWithCollectionsSizeOnly(
            captor -> verify(attributeHistoryService, times(2)).upsertCandidateValues(captor.capture()),
            List.of(expectedCreatedArtistAttrValuesNumber, expectedCreatedTrackAttrValuesNumber),
            "attributeHistoryService.upsertCandidateValues"
        );
    }

    /**
     * Test dto root example contains three tracks, two of which belong to the same artist, to test deduplication
     */
    private final static String TEST_DTO_ROOT = """
        {
          "tracks": {
            "track": [
              {
                "name": "Smells Like Teen Spirit",
                "duration": "301",
                "mbid": "0ebe2d92-a11d-4b2b-9922-806383074ed7",
                "url": "https://www.last.fm/music/Nirvana/_/Smells+Like+Teen+Spirit",
                "streamable": {
                  "#text": "0",
                  "fulltrack": "0"
                },
                "artist": {
                  "name": "Nirvana",
                  "mbid": "9282c8b4-ca0b-4c6b-b7e3-4f7762dfc4d6",
                  "url": "https://www.last.fm/music/Nirvana"
                },
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  }
                ],
                "@attr": {
                  "rank": "1"
                }
              },
              {
                "name": "Creep",
                "duration": "239",
                "mbid": "d11fcceb-dfc5-4d19-b45d-f4e8f6d3eaa6",
                "url": "https://www.last.fm/music/Radiohead/_/Creep",
                "streamable": {
                  "#text": "0",
                  "fulltrack": "0"
                },
                "artist": {
                  "name": "Radiohead",
                  "mbid": "a74b1b7f-71a5-4011-9441-d0b5e4122711",
                  "url": "https://www.last.fm/music/Radiohead"
                },
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  }
                ],
                "@attr": {
                  "rank": "3"
                }
              },
              {
                "name": "Come as You Are",
                "duration": "208",
                "mbid": "e05035a3-14ac-4f88-a160-0a144530004e",
                "url": "https://www.last.fm/music/Nirvana/_/Come+as+You+Are",
                "streamable": {
                  "#text": "0",
                  "fulltrack": "0"
                },
                "artist": {
                  "name": "Nirvana",
                  "mbid": "9282c8b4-ca0b-4c6b-b7e3-4f7762dfc4d6",
                  "url": "https://www.last.fm/music/Nirvana"
                },
                "image": [
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/34s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "small"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/64s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "medium"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/174s/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "large"
                  },
                  {
                    "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                    "size": "extralarge"
                  }
                ],
                "@attr": {
                  "rank": "4"
                }
              }
            ],
            "@attr": {
              "tag": "rock",
              "page": "1",
              "perPage": "50",
              "totalPages": "10385",
              "total": "519209"
            }
          }
        }
        """;

    @AllArgsConstructor
    private static class TestCase {
        final LastfmApiCall sourceApiCall;
        final LastfmApiResponse sourceApiResponse;
        final List<LastfmTrack> expectedTracks;
        final List<LastfmArtist> expectedArtists;
    }

    private TestCase testCaseFromResponse(String responseString) {
        LastfmTag scopeEntity = consistencyHelper.createAndSaveTag(LastfmApiCallType.TAG_TOP_ARTISTS);
        LastfmApiResponse sourceApiResponse = consistencyHelper.createDummyApiResponse(
            responseString, LastfmApiCallType.TAG_TOP_TRACKS, scopeEntity);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TagTopTracksDtoRoot dtoRoot = objectMapper.readValue(responseString, TagTopTracksDtoRoot.class);

            LastfmTrackEntityFactory<TagTopTracksTrackDto> trackFactory = new TagTopTracksTrackFactory();
            List<LastfmTrack> expectedTracks = dtoRoot.getRootObject().getTracks().stream()
                    .map(track -> trackFactory.fromDto(track, sourceApiResponse))
                .toList();

            LastfmArtistEntityFactory<TagTopTracksTrackArtistDto> artistFactory = new LastfmArtistEntityFactory<>();
            List<LastfmArtist> expectedArtists = dtoRoot.getRootObject().getTracks().stream()
                    .map(TagTopTracksTrackDto::getArtist)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(artistDto -> artistFactory.fromDto(artistDto, sourceApiResponse))
                .toList();

            return new TestCase(sourceApiResponse.getApiCall(), sourceApiResponse, expectedTracks, expectedArtists);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}