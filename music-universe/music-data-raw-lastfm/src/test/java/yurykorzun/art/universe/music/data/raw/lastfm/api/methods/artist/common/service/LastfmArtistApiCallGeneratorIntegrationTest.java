package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.service.LastfmArtistGetInfoApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.service.LastfmArtistGetSimilarApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.service.LastfmArtistTopAlbumsApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.service.LastfmArtistTopTagsApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.service.LastfmArtistTopTracksApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.ExpectedAttributeSnapshotInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils.*;

/**
 * <p>This class wraps common logic of 'artist.[method]' api calls generator tests.</p>
 * <p>Currently supported tests are:
 * <ul>
 *     <li>check that generator returns correct API call type</li>
 *     <li>check that generator generates api calls correctly when artists and api calls lists are empty</li>
 *     <li>TODO test generator behaviour when API calls exist, artist is not approved etc.</li>
 * </ul>
 * </p>
 * <p>To test new generator, do the following:
 * <ul>
 *     <li>add generator class to @Import</li>
 *     <li>add Arguments instance to parameterProvider method</li>
 * </ul>
 * </p>
 */
@Tag("integration")
@Import({
    LastfmArtistGetInfoApiCallGenerator.class,
    LastfmArtistTopAlbumsApiCallGenerator.class,
    LastfmArtistTopTracksApiCallGenerator.class,
    LastfmArtistTopTagsApiCallGenerator.class,
    LastfmArtistGetSimilarApiCallGenerator.class,
})
public class LastfmArtistApiCallGeneratorIntegrationTest extends JpaOnlyTest {

    @MockitoBean
    protected LastfmApiCallService apiCallService;
    @MockitoBean
    protected LastfmDataSnapshotService dataSnapshotService;
    @MockitoBean
    protected LastfmAttributeSnapshotService attributeSnapshotService;
    @MockitoBean
    protected LastfmEntityService entityService;
    @MockitoBean
    protected LastfmArtistService artistService;

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    protected DbConsistencyHelper consistencyHelper;

    /**
     * Test parameters set contains:
     * <ol>
     *     <li>{@link LastfmArtistApiCallGenerator} class. Instance will be retrieved from {@link ApplicationContext} in runtime,
     *     that is why we need @Import annotation on top of the class</li>
     *     <li>corresponding {@link LastfmApiCallType} (will be used in getApiCallType() test)</li>
     *     <li>list of {@link ExpectedAttributeSnapshotInfo} instances representing attribute snapshots created by generator</li>
     * </ol>
     */
    static Stream<Arguments> parametersProvider() {
        return Stream.of(
            Arguments.of(
                LastfmArtistGetInfoApiCallGenerator.class,
                LastfmApiCallType.ARTIST_GET_INFO,
                List.<ExpectedAttributeSnapshotInfo>of()
            ),
            Arguments.of(
                LastfmArtistTopAlbumsApiCallGenerator.class,
                LastfmApiCallType.ARTIST_TOP_ALBUMS,
                List.<ExpectedAttributeSnapshotInfo>of()
            ),
            Arguments.of(
                LastfmArtistTopTracksApiCallGenerator.class,
                LastfmApiCallType.ARTIST_TOP_TRACKS,
                List.<ExpectedAttributeSnapshotInfo>of()
            ),
            Arguments.of(
                LastfmArtistTopTagsApiCallGenerator.class,
                LastfmApiCallType.ARTIST_TOP_TAGS,
                List.of(
                    new ExpectedAttributeSnapshotInfo(LastfmAttribute.RANK, LastfmEntityType.TAG)
                )
            ),
            Arguments.of(
                LastfmArtistGetSimilarApiCallGenerator.class,
                LastfmApiCallType.ARTIST_GET_SIMILAR,
                List.<ExpectedAttributeSnapshotInfo>of()
            )
        );
    }

    protected static final int UNPROCESSED_ARTISTS_COUNT = 3;
    private static final int DUE_DURATION_DAYS = 1;

    protected LastfmApiCall createArtistSourceApiCall(boolean isExpired) {
        return consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.TAG_TOP_ARTISTS)
            .dueDttm(Instant.now().plus(Duration.ofDays(isExpired ? -1 : 1)))
            .params(Map.of())
        );
    }

    private LastfmArtistApiCallGenerator getGenerator(Class<? extends LastfmArtistApiCallGenerator> clazz) {
        LastfmArtistApiCallGenerator generator = ctx.getBean(clazz);
        ReflectionTestUtils.setField(generator, "dueDurationDays", DUE_DURATION_DAYS);
        return generator;
    }

    @ParameterizedTest(name = "[{index}]getApiCallType({1})")
    @MethodSource("parametersProvider")
    void getApiCallType_shouldReturnExpectedType(
        Class<? extends LastfmArtistApiCallGenerator>  generatorClass,
        LastfmApiCallType                               expectedApiCallType,
        List<ExpectedAttributeSnapshotInfo>             ignoredAttrSnapshots
    ) {
        LastfmArtistApiCallGenerator generator = getGenerator(generatorClass);

        LastfmApiCallType type = generator.getApiCallType();
        assertNotNull(type, "Generator api call type must not be null");
        assertEquals(expectedApiCallType, type, "Unexpected generator api call type");
    }

    @ParameterizedTest(name = "[{index}] createApiCalls for {0}")
    @MethodSource("parametersProvider")
    void createApiCalls_shouldCreateSnapshotsAndApiCalls_whenArtistProvided(
        Class<? extends LastfmArtistApiCallGenerator>  generatorClass,
        LastfmApiCallType                               ignoredApiCallType,
        List<ExpectedAttributeSnapshotInfo>             expectedAttrSnapshots
    ) {
        LastfmArtistApiCallGenerator generator = getGenerator(generatorClass);

        // given
        LastfmApiCall apiCall = createArtistSourceApiCall(true);
        List<LastfmArtist> unprocessedArtists = createUnprocessedArtists(apiCall);
        mockUnprocessedArtistRetrieval(generator, unprocessedArtists);

        // when
        generator.createApiCalls();

        // check that data snapshot was requested and its created counter was incremented
        if (generator.getApiCallType().getScopeEntityType() != null) {
            for (LastfmArtist artist : unprocessedArtists) {
                verify(dataSnapshotService).getOrCreateSnapshotFor(eq(generator.getApiCallType()), eq(artist));
            }
            verifyInvocationsNumberWithCollectionsSizeOnly(
                captor -> verify(dataSnapshotService).incCreatedCount(captor.capture()),
                List.of(UNPROCESSED_ARTISTS_COUNT),
                "dataSnapshotService.incCreatedCount"
            );
        }

        // check that attribute snapshots were created
        final boolean areAttrSnapshotsExpected = !expectedAttrSnapshots.isEmpty();
        for (LastfmArtist artist : unprocessedArtists) {
            if (areAttrSnapshotsExpected) {
                for (ExpectedAttributeSnapshotInfo snapshot : expectedAttrSnapshots) {
                    verify(attributeSnapshotService).getOrCreateForEntity(
                        any(), eq(snapshot.getTargetEntityType()), eq(snapshot.getAttribute()), eq(artist));
                }
            } else {
                verify(attributeSnapshotService, never()).getOrCreateForEntity(any(), any(), any(), eq(artist));
            }
        }

        // check that api calls creation was initiated
        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        List<LastfmApiCallCreateRequest> capturedCalls = captor.getValue();

        // we expect as many api calls as many artists were unprocessed
        assertEquals(UNPROCESSED_ARTISTS_COUNT, capturedCalls.size(),
            String.format("Expected %s new api calls, got %s", UNPROCESSED_ARTISTS_COUNT, capturedCalls.size()));

        // check api calls parameters
        capturedCalls.forEach(request -> {
            Map<String, String> params = request.getParams();

            // check that entity type and id are present and correct
            assertEquals(LastfmEntityType.ARTIST, request.getEntityType());
            assertTrue(request.getEntityId() > 0);

            // check that call parameters contain artist name
            String artistName = params.get(LastfmApiConstants.PARAM_NAME_ARTIST);
            String artistMbid = params.get(LastfmApiConstants.PARAM_NAME_MBID);
            boolean isNamePresent = artistName != null;
            boolean isMbidPresent = artistMbid != null;
            // check that only one value is present
            assertTrue(isNamePresent != isMbidPresent);
            if (isNamePresent) {
                assertTrue(artistName.startsWith("artist"), "artist name in api call parameters is incorrect");
            } else {
                assertTrue(artistMbid.startsWith("mbid"), "artist mbid in api call parameters is incorrect");
            }

            // check that call parameters contain autocorrect disabled
            String autocorrect = params.get(LastfmApiConstants.PARAM_NAME_AUTOCORRECT);
            assertNotNull(autocorrect, "'autocorrect' parameter must be present in api call parameters'");
            assertEquals("0", autocorrect);

            // check that call parameters contain correct page limit
            if (params.containsKey(LastfmApiConstants.PARAM_NAME_LIMIT)) {
                assertEquals(String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE), params.get(LastfmApiConstants.PARAM_NAME_LIMIT));
            }
        });
    }

    private List<LastfmArtist> createUnprocessedArtists(LastfmApiCall apiCall) {
        return IntStream.range(0, UNPROCESSED_ARTISTS_COUNT)
            .mapToObj(i -> {
                LastfmArtist artist = LastfmArtist.builder()
                    .name(String.format("artist-%d", i))
                    .mbid(i % 2 == 0 ? null : String.format("mbid-%d", i))
                    .apiCall(apiCall)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .build();
                ReflectionTestUtils.setField(artist, "id", i + 1);
                return artist;
            })
            .collect(Collectors.toList());
    }

    private void mockUnprocessedArtistRetrieval(LastfmArtistApiCallGenerator generator, List<LastfmArtist> unprocessedArtists) {
        when(artistService.findAllToGetInfoFor()).thenReturn(unprocessedArtists);
        when(entityService.<LastfmArtist>findAllUnprocessed(eq(LastfmEntityType.ARTIST), eq(generator.getApiCallType()), any()))
            .thenReturn(unprocessedArtists);
        when(dataSnapshotService.getOrCreateSnapshotFor(eq(generator.getApiCallType()), any(LastfmArtist.class)))
            .thenAnswer(invocation -> {
                LastfmApiCallType apiCallType = invocation.getArgument(0);
                LastfmArtist artist = invocation.getArgument(1);
                LastfmDataSnapshot snapshot = new LastfmDataSnapshot(apiCallType, LocalDate.now(), artist);
                ReflectionTestUtils.setField(snapshot, "id", artist.getId());
                return snapshot;
            });
        when(dataSnapshotService.getOrCreateSnapshotFor(eq(generator.getApiCallType())))
            .thenAnswer(invocation -> {
                LastfmApiCallType apiCallType = invocation.getArgument(0);
                LastfmDataSnapshot snapshot = new LastfmDataSnapshot(apiCallType, LocalDate.now());
                ReflectionTestUtils.setField(snapshot, "id", 1);
                return snapshot;
            });
    }
}