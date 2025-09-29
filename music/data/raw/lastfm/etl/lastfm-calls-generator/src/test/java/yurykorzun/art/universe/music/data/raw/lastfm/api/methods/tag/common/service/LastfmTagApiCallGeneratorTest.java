package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmTagApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @Test
    void isValidForApiCall_shouldReturnTrue_whenTagHasName() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder.name("rock"));

        // when
        boolean result = generator.isValidForApiCall(tag);

        // then
        assertTrue(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenTagHasNoName() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder.name(null));

        // when
        boolean result = generator.isValidForApiCall(tag);

        // then
        assertFalse(result);
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenTagHasEmptyName() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder.name(""));

        // when
        boolean result = generator.isValidForApiCall(tag);

        // then
        assertFalse(result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnTagName() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder.name("rock"));

        // when
        String result = generator.getApiCallUniqueKey(tag);

        // then
        assertEquals("rock", result);
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenTagHasNoName() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder.name(null));

        // when
        String result = generator.getApiCallUniqueKey(tag);

        // then
        assertNull(result);
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag approved = EntityCreationHelper.createTag(builder -> builder.approvalStatus(ApprovalStatus.APPROVED));
        LastfmTag pending = EntityCreationHelper.createTag(builder -> builder.approvalStatus(ApprovalStatus.PENDING));

        // when & then
        assertTrue(generator.hasHigherPriority(approved, pending));
        assertFalse(generator.hasHigherPriority(pending, approved));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByUsageUsersCount_whenSameApprovalStatus() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag moreUsers = EntityCreationHelper.createTag(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).usageUsersCount(1000));
        LastfmTag lessUsers = EntityCreationHelper.createTag(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).usageUsersCount(500));

        // when & then
        assertTrue(generator.hasHigherPriority(moreUsers, lessUsers));
        assertFalse(generator.hasHigherPriority(lessUsers, moreUsers));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByUsageCount_whenUsageUsersCountEqual() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag moreUsage = EntityCreationHelper.createTag(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).usageUsersCount(1000).usageCount(5000));
        LastfmTag lessUsage = EntityCreationHelper.createTag(builder -> 
            builder.approvalStatus(ApprovalStatus.APPROVED).usageUsersCount(1000).usageCount(3000));

        // when & then
        assertTrue(generator.hasHigherPriority(moreUsage, lessUsage));
        assertFalse(generator.hasHigherPriority(lessUsage, moreUsage));
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByLowerId_whenAllElseEqual() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag older = EntityCreationHelper.createTag(builder -> 
            builder.id(1L).approvalStatus(ApprovalStatus.APPROVED).usageUsersCount(1000).usageCount(5000));
        LastfmTag newer = EntityCreationHelper.createTag(builder -> 
            builder.id(2L).approvalStatus(ApprovalStatus.APPROVED).usageUsersCount(1000).usageCount(5000));

        // when & then
        assertTrue(generator.hasHigherPriority(older, newer));
        assertFalse(generator.hasHigherPriority(newer, older));
    }

    @Test
    void getCommonApiCallParameters_shouldUseTagName() {
        // given
        TestableTagGenerator generator = new TestableTagGenerator(apiCallService, snapshotService, entityService);
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder.name("rock"));

        // when
        Map<String, String> result = generator.getCommonApiCallParameters(tag);

        // then
        assertEquals("rock", result.get("tag"));
        assertEquals(1, result.size());
    }

    private static class TestableTagGenerator extends LastfmTagApiCallGenerator {
        public TestableTagGenerator(LastfmApiCallService apiCallService, LastfmDataSnapshotService snapshotService, LastfmApiCallEntityService entityService) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.TAG_TOP_TAGS;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }
    }
}
