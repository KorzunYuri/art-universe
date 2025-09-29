package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmTrackApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @Test
    void isValidForApiCall_shouldReturnTrue_whenTrackHasMbid() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> builder.mbid("test-mbid"));

        // when
        boolean result = generator.isValidForApiCall(track);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenTrackHasNameAndValidArtist() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> 
            builder.name("Test Track").artist(artist));

        // when
        boolean result = generator.isValidForApiCall(track);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenTrackHasNoMbidAndNoArtist() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> 
            builder.mbid(null).artist(null));

        // when
        boolean result = generator.isValidForApiCall(track);

        // then
        assertFalse(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenTrackHasNoMbidAndArtistHasNoName() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name(null));
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> 
            builder.mbid(null).artist(artist));

        // when
        boolean result = generator.isValidForApiCall(track);

        // then
        assertFalse(result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnMbidKey_whenMbidExists() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> builder.mbid("test-mbid"));

        // when
        String result = generator.getApiCallUniqueKey(track);

        // then
        assertEquals("mbid-test-mbid", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNamesKey_whenMbidMissingButArtistAndTrackNamesExist() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> 
            builder.mbid(null).name("Test Track").artist(artist));

        // when
        String result = generator.getApiCallUniqueKey(track);

        // then
        assertEquals("names-Test Artist-Test Track", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenNoValidIdentifier() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> 
            builder.mbid(null).artist(null));

        // when
        String result = generator.getApiCallUniqueKey(track);

        // then
        assertNull(result);
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack approved = EntityCreationHelper.createTrack(builder -> builder.approvalStatus(ApprovalStatus.APPROVED));
        LastfmTrack pending = EntityCreationHelper.createTrack(builder -> builder.approvalStatus(ApprovalStatus.PENDING));

        // when & then
        assertTrue(generator.hasHigherPriority(approved, pending));
        assertFalse(generator.hasHigherPriority(pending, approved));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByTrackListenersCount_whenSameApprovalStatus() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack morePopular = EntityCreationHelper.createTrack(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000));
        LastfmTrack lessPopular = EntityCreationHelper.createTrack(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500));

        // when & then
        assertTrue(generator.hasHigherPriority(morePopular, lessPopular));
        assertFalse(generator.hasHigherPriority(lessPopular, morePopular));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByArtistListenersCount_whenTrackMetricsEqual() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist popularArtist = EntityCreationHelper.createArtist(builder -> builder.listenersCount(2000));
        LastfmArtist lessPopularArtist = EntityCreationHelper.createArtist(builder -> builder.listenersCount(1000));
        
        LastfmTrack trackWithPopularArtist = EntityCreationHelper.createTrack(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500).artist(popularArtist));
        LastfmTrack trackWithLessPopularArtist = EntityCreationHelper.createTrack(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500).artist(lessPopularArtist));

        // when & then
        assertTrue(generator.hasHigherPriority(trackWithPopularArtist, trackWithLessPopularArtist));
        assertFalse(generator.hasHigherPriority(trackWithLessPopularArtist, trackWithPopularArtist));
    }

    @Test
    void getCommonApiCallParameters_shouldUseMbid_whenMbidExists() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> builder.mbid("test-mbid"));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(track);

        // then
        assertEquals("test-mbid", result.get("mbid"));
        assertFalse(result.containsKey("track"));
        assertFalse(result.containsKey("artist"));
    }

    @Test
    void getCommonApiCallParameters_shouldUseTrackAndArtistNames_whenMbidMissing() {
        // given
        TestableTrackGenerator generator = new TestableTrackGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> 
            builder.mbid(null).name("Test Track").artist(artist));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(track);

        // then
        assertEquals("Test Track", result.get("track"));
        assertEquals("Test Artist", result.get("artist"));
        assertFalse(result.containsKey("mbid"));
    }

    private static class TestableTrackGenerator extends LastfmTrackApiCallGenerator {
        public TestableTrackGenerator(LastfmApiCallService apiCallService, LastfmDataSnapshotService snapshotService, LastfmApiCallEntityService entityService) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.TRACK_GET_INFO;
        }

        @Override
        protected int getDueDurationDays() {
            return 28;
        }
    }
}
