package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @Test
    void isValidForApiCall_shouldReturnTrue_whenAlbumHasMbid() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> builder.mbid("test-mbid"));

        // when
        boolean result = generator.isValidForApiCall(album);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenAlbumHasNameAndValidArtist() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> 
            builder.name("Test Album").artist(artist));

        // when
        boolean result = generator.isValidForApiCall(album);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenAlbumHasNoMbidAndNoArtist() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> 
            builder.mbid(null).artist(null));

        // when
        boolean result = generator.isValidForApiCall(album);

        // then
        assertFalse(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenAlbumHasNoMbidAndArtistHasNoName() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name(null));
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> 
            builder.mbid(null).artist(artist));

        // when
        boolean result = generator.isValidForApiCall(album);

        // then
        assertFalse(result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnMbidKey_whenMbidExists() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> builder.mbid("test-mbid"));

        // when
        String result = generator.getApiCallUniqueKey(album);

        // then
        assertEquals("mbid-test-mbid", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNamesKey_whenMbidMissingButArtistAndAlbumNamesExist() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> 
            builder.mbid(null).name("Test Album").artist(artist));

        // when
        String result = generator.getApiCallUniqueKey(album);

        // then
        assertEquals("names-Test Artist-Test Album", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenNoValidIdentifier() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> 
            builder.mbid(null).artist(null));

        // when
        String result = generator.getApiCallUniqueKey(album);

        // then
        assertNull(result);
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum approved = EntityCreationHelper.createAlbum(builder -> builder.approvalStatus(ApprovalStatus.APPROVED));
        LastfmAlbum pending = EntityCreationHelper.createAlbum(builder -> builder.approvalStatus(ApprovalStatus.PENDING));

        // when & then
        assertTrue(generator.hasHigherPriority(approved, pending));
        assertFalse(generator.hasHigherPriority(pending, approved));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByAlbumListenersCount_whenSameApprovalStatus() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum morePopular = EntityCreationHelper.createAlbum(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(1000));
        LastfmAlbum lessPopular = EntityCreationHelper.createAlbum(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500));

        // when & then
        assertTrue(generator.hasHigherPriority(morePopular, lessPopular));
        assertFalse(generator.hasHigherPriority(lessPopular, morePopular));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByArtistListenersCount_whenAlbumMetricsEqual() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist popularArtist = EntityCreationHelper.createArtist(builder -> builder.listenersCount(2000));
        LastfmArtist lessPopularArtist = EntityCreationHelper.createArtist(builder -> builder.listenersCount(1000));
        
        LastfmAlbum albumWithPopularArtist = EntityCreationHelper.createAlbum(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500).artist(popularArtist));
        LastfmAlbum albumWithLessPopularArtist = EntityCreationHelper.createAlbum(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).listenersCount(500).artist(lessPopularArtist));

        // when & then
        assertTrue(generator.hasHigherPriority(albumWithPopularArtist, albumWithLessPopularArtist));
        assertFalse(generator.hasHigherPriority(albumWithLessPopularArtist, albumWithPopularArtist));
    }

    @Test
    void getCommonApiCallParameters_shouldUseMbid_whenMbidExists() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> builder.mbid("test-mbid"));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(album);

        // then
        assertEquals("test-mbid", result.get("mbid"));
        assertFalse(result.containsKey("album"));
        assertFalse(result.containsKey("artist"));
    }

    @Test
    void getCommonApiCallParameters_shouldUseAlbumAndArtistNames_whenMbidMissing() {
        // given
        TestableAlbumGenerator generator = new TestableAlbumGenerator(apiCallService, snapshotService, entityService);
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> 
            builder.mbid(null).name("Test Album").artist(artist));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(album);

        // then
        assertEquals("Test Album", result.get("album"));
        assertEquals("Test Artist", result.get("artist"));
        assertFalse(result.containsKey("mbid"));
    }

    private static class TestableAlbumGenerator extends LastfmAlbumApiCallGenerator {
        public TestableAlbumGenerator(LastfmApiCallService apiCallService, LastfmDataSnapshotService snapshotService, LastfmApiCallEntityService entityService) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ALBUM_GET_INFO;
        }

        @Override
        protected int getDueDurationDays() {
            return 28;
        }
    }
}
