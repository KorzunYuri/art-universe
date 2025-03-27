package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepository repository;


    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    private LastfmDataSnapshot createDummyDataSnapshot() {
        return snapshotRepository.save(new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, new Date()));
    }

    private LastfmApiCall createDummyApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot();
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_ARTISTS)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
                .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    private LastfmTag createTag(String rock, LastfmApiCall apiCall) {
        return LastfmTag.builder()
                .name(rock)
                .apiCall(apiCall)
            .build();
    }

    @Test
    void testFindAllByNameIn() {
        // given
        LastfmApiCall apiCall = createDummyApiCall();
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
        LastfmApiCall apiCall = createDummyApiCall();
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