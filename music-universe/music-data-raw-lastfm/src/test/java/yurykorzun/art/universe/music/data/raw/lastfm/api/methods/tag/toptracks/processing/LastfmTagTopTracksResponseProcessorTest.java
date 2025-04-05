package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private LastfmArtistRepository artistRepository;
    @MockitoBean
    private LastfmTrackRepository trackRepository;

    // the variables below depend on currently supported attributes and should change along with processor implementation
    private static final int TRACK_SCD2_ATTRS_NUMBER = 4;
    private static final int TRACK_SNAPSHOT_ATTRS_NUMBER = 1;
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

        TestCase testCase = testCaseFromDtoString(TEST_DTO_ROOT);

        final int expectedCreatedTracksNumber = testCase.expectedTracks.size();
        final int expectedCreatedArtistsNumber = testCase.expectedArtists.size();
        final int expectedCreatedTrackAttrValuesNumber = expectedCreatedTracksNumber * TRACK_ATTRS_NUMBER;
        final int expectedCreatedArtistAttrValuesNumber = expectedCreatedArtistsNumber * ARTIST_ATTRS_NUMBER;
        final int expectedCreatedAttrValuesNumber = expectedCreatedTrackAttrValuesNumber + expectedCreatedArtistAttrValuesNumber;

        // when(trackRepository.findAllByUrlIn(testCase.getTrackUrls())).thenReturn(List.of());
        when(trackRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        // when(artistRepository.findAllByNameIn(testCase.getArtistNames())).thenReturn(List.of());
        when(artistRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        when(attributeHistoryService.upsertCandidateValue(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        processor.processResponse(testCase.sourceApiResponse);

        // Verify that trackRepository.findAllByUrlIn was called with the correct set of track urls.
        ArgumentCaptor<List<String>> urlsCaptor = ArgumentCaptor.forClass(List.class);
        verify(trackRepository, times(1)).findAllByUrlIn(urlsCaptor.capture());
        List<String> capturedUrls = urlsCaptor.getValue();
        assertEquals(expectedCreatedTracksNumber, capturedUrls.size(),
            String.format("Expected %d track urls to be searched", expectedCreatedTracksNumber));

        // Verify that new tracks are saved
        ArgumentCaptor<List<LastfmTrack>> trackCaptor = ArgumentCaptor.forClass(List.class);
        verify(trackRepository, times(1)).saveAll(trackCaptor.capture());
        List<LastfmTrack> savedTracks = trackCaptor.getValue();
        assertEquals(expectedCreatedTracksNumber, savedTracks.size(),
            String.format("Expected %d new tracks to be saved", expectedCreatedTracksNumber));
        assertEquals(testCase.expectedTracks, savedTracks);

        // Verify that artistRepository.findAllByNameIn was called with the correct set of artist names.
        ArgumentCaptor<List<String>> namesCaptor = ArgumentCaptor.forClass(List.class);
        verify(artistRepository, times(1)).findAllByNameIn(namesCaptor.capture());
        List<String> capturedNames = namesCaptor.getValue();
        assertEquals(expectedCreatedArtistsNumber, capturedNames.size(),
            String.format("Expected %d artist names to be searched", expectedCreatedTracksNumber));

        // Verify that new artists are saved
        ArgumentCaptor<List<LastfmArtist>> artistsCaptor = ArgumentCaptor.forClass(List.class);
        verify(artistRepository, times(1)).saveAll(artistsCaptor.capture());
        List<LastfmArtist> savedArtists = artistsCaptor.getValue();
        assertEquals(expectedCreatedArtistsNumber, savedArtists.size(),
            String.format("Expected %d new artists to be saved", expectedCreatedArtistsNumber));
        assertEquals(testCase.expectedArtists, savedArtists);

        // Verify that entity relations are created.
        ArgumentCaptor<List<LastfmEntityRelation>> relCaptor = ArgumentCaptor.forClass(List.class);
        verify(entityRelationService, times(2)).upsertEntityRelations(relCaptor.capture());
        List<List<LastfmEntityRelation>> saveRelationsInvocationParams = relCaptor.getAllValues();
        assertEquals(expectedCreatedTracksNumber, saveRelationsInvocationParams.get(0).size(),
            String.format("Expected %d tag-track relations to be created", expectedCreatedTracksNumber));
        assertEquals(expectedCreatedTracksNumber, saveRelationsInvocationParams.get(1).size(),
            String.format("Expected %d artist-track relations to be created", expectedCreatedTracksNumber));

        // Verify that attribute history records are upserted.
        ArgumentCaptor<List<LastfmAttributeHistoryRecord>> attrCaptor = ArgumentCaptor.forClass(List.class);
        verify(attributeHistoryService, times(2)).upsertCandidateValues(attrCaptor.capture());
        List<List<LastfmAttributeHistoryRecord>> saveAttrValuesInvocationParams = attrCaptor.getAllValues();
        assertEquals(expectedCreatedArtistAttrValuesNumber, saveAttrValuesInvocationParams.get(0).size(),
            String.format("Expected %d artist attr values to be created", expectedCreatedArtistAttrValuesNumber));
        assertEquals(expectedCreatedTrackAttrValuesNumber, saveAttrValuesInvocationParams.get(1).size(),
            String.format("Expected %d track attr values to be created", expectedCreatedTrackAttrValuesNumber));
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

        List<String> getTrackUrls() {
            return expectedTracks.stream().map(LastfmTrack::getUrl).toList();
        }

        List<String> getArtistNames() {
            return expectedArtists.stream().map(LastfmArtist::getName).toList();
        }
    }

    private TestCase testCaseFromDtoString(String dtoString) {
        LastfmTag scopeEntity = consistencyHelper.createAndSaveTag(LastfmApiCallType.TAG_TOP_ARTISTS);
        LastfmApiResponse sourceApiResponse = consistencyHelper.createDummyApiResponse(
            dtoString, LastfmApiCallType.TAG_TOP_TRACKS, scopeEntity);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TagTopTracksDtoRoot dtoRoot = objectMapper.readValue(dtoString, TagTopTracksDtoRoot.class);

            LastfmTrackEntityFactory trackFactory = new LastfmTrackEntityFactory();
            List<LastfmTrack> expectedTracks = dtoRoot.getRootObject().getTracks().stream()
                    .map(track -> trackFactory.fromDto(track, sourceApiResponse))
                .toList();

            LastfmArtistEntityFactory artistFactory = new LastfmArtistEntityFactory();
            List<LastfmArtist> expectedArtists = dtoRoot.getRootObject().getTracks().stream()
                    .map(TrackDto::getArtist)
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