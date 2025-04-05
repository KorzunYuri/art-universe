package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepository repository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    private LastfmTag createTag(String name, LastfmApiCall apiCall) {
        return LastfmTag.builder()
                .name(name)
                .usageCount(42)
                .usageUsersCount(10)
                .apiCall(apiCall)
            .build();
    }

    @Test
    void testSaveTag() {
        final String name = "Tag";
        final int usageCount = 42;
        final int usageUsersCount = 10;
        final LastfmApiCall sourceApiCall = consistencyHelper.createDummyApiCall(LastfmApiCallType.TAG_TOP_TAGS);

        LastfmTag tag = LastfmTag.builder()
                .name(name)
                .usageCount(usageCount)
                .usageUsersCount(usageUsersCount)
                .apiCall(sourceApiCall)
            .build();

        LastfmTag saved = repository.save(tag);
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals(name, saved.getName());
        assertEquals(usageCount, saved.getUsageCount());
        assertEquals(usageUsersCount, saved.getUsageUsersCount());
        assertEquals(sourceApiCall, saved.getApiCall());
    }

    @Test
    void testFindAllByNameIn() {
        // given
        LastfmApiCall apiCall = consistencyHelper.createDummyApiCall();
        LastfmTag tag1 = createTag("rock", apiCall);
        LastfmTag tag2 = createTag("pop", apiCall);
        repository.saveAllAndFlush(Arrays.asList(tag1, tag2));

        // when
        List<LastfmTag> foundTags = repository.findAllByNameIn(Collections.singletonList("rock"));

        // then
        assertEquals(1, foundTags.size(), "Only one tag should be found by name");
        assertEquals("rock", foundTags.get(0).getName(), "Tag name persisted incorrectly");
    }

    @Test
    void testSaveAllDoesNotDuplicate() {
        // given
        LastfmApiCall apiCall = consistencyHelper.createDummyApiCall();
        LastfmTag tag1 = createTag("jazz", apiCall);
        LastfmTag tag2 = createTag("latina", apiCall);
        repository.saveAllAndFlush(Arrays.asList(tag1, tag2));

        // when
        LastfmTag tag3 = createTag("metal", apiCall);
        repository.saveAllAndFlush(Arrays.asList(tag1, tag3)); // one new, one existing

        // then
        List<LastfmTag> allTags = repository.findAll();
        assertEquals(3, allTags.size(), "Second save of the same tag should not produce duplicates");
    }

}