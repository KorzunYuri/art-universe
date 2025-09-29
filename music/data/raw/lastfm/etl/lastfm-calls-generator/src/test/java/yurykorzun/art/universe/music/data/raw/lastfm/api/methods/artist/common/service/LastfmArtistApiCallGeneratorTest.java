package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @Test
    void isValidForApiCall_shouldReturnTrue_whenArtistHasMbid() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.mbid("test-mbid"));

        // when
        boolean result = generator.isValidForApiCall(artist);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenArtistHasName() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));

        // when
        boolean result = generator.isValidForApiCall(artist);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenArtistHasNeitherMbidNorName() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name(null).mbid(null));

        // when
        boolean result = generator.isValidForApiCall(artist);

        // then
        assertFalse(result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnMbidKey_whenMbidExists() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> 
            builder.mbid("test-mbid").name("Test Artist"));

        // when
        String result = generator.getApiCallUniqueKey(artist);

        // then
        assertEquals("mbid-test-mbid", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNameKey_whenOnlyNameExists() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));

        // when
        String result = generator.getApiCallUniqueKey(artist);

        // then
        assertEquals("names-Test Artist", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenNeitherMbidNorNameExists() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name(null).mbid(null));

        // when
        String result = generator.getApiCallUniqueKey(artist);

        // then
        assertNull(result);
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist approved = EntityCreationHelper.createArtist(builder -> builder.approvalStatus(ApprovalStatus.APPROVED));
        LastfmArtist pending = EntityCreationHelper.createArtist(builder -> builder.approvalStatus(ApprovalStatus.PENDING));

        // when & then
        assertTrue(generator.hasHigherPriority(approved, pending));
        assertFalse(generator.hasHigherPriority(pending, approved));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByListenersCount_whenSameApprovalStatus() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist morePopular = EntityCreationHelper.createArtist(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000));
        LastfmArtist lessPopular = EntityCreationHelper.createArtist(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500));

        // when & then
        assertTrue(generator.hasHigherPriority(morePopular, lessPopular));
        assertFalse(generator.hasHigherPriority(lessPopular, morePopular));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByPlayCount_whenSameApprovalStatusAndListeners() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist morePlayCount = EntityCreationHelper.createArtist(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000).playCount(5000L));
        LastfmArtist lessPlayCount = EntityCreationHelper.createArtist(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000).playCount(3000L));

        // when & then
        assertTrue(generator.hasHigherPriority(morePlayCount, lessPlayCount));
        assertFalse(generator.hasHigherPriority(lessPlayCount, morePlayCount));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByLowerId_whenAllElseEqual() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist older = EntityCreationHelper.createArtist(builder -> 
            builder.id(1L).approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000).playCount(5000L));
        LastfmArtist newer = EntityCreationHelper.createArtist(builder -> 
            builder.id(2L).approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000).playCount(5000L));

        // when & then
        assertTrue(generator.hasHigherPriority(older, newer));
        assertFalse(generator.hasHigherPriority(newer, older));
    }

    @Test
    void getCommonApiCallParameters_shouldUseMbid_whenMbidExists() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> 
            builder.mbid("test-mbid").name("Test Artist"));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(artist);

        // then
        assertEquals("test-mbid", result.get("mbid"));
        assertEquals("0", result.get("autocorrect"));
        assertFalse(result.containsKey("artist"));
    }

    @Test
    void getCommonApiCallParameters_shouldUseName_whenMbidMissing() {
        // given
        TestableArtistGenerator generator = new TestableArtistGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(artist);

        // then
        assertEquals("Test Artist", result.get("artist"));
        assertEquals("0", result.get("autocorrect"));
        assertFalse(result.containsKey("mbid"));
    }

    private static class TestableArtistGenerator extends LastfmArtistApiCallGenerator {
        public TestableArtistGenerator(LastfmApiCallService apiCallService, LastfmDataSnapshotService snapshotService, LastfmApiCallEntityService entityService) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ARTIST_GET_INFO;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }
    }
}
