package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepository repository;

    @Test
    void testFindAllByNameIn() {
        // given
        LastfmTag tag1 = LastfmTag.builder().name("rock").build();
        LastfmTag tag2 = LastfmTag.builder().name("pop").build();
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
        LastfmTag tag1 = LastfmTag.builder().name("jazz").build();
        LastfmTag tag2 = LastfmTag.builder().name("latina").build();
        repository.saveAllAndFlush(Arrays.asList(tag1, tag2));

        // when
        LastfmTag tag3 = LastfmTag.builder().name("metal").build();
        repository.saveAllAndFlush(Arrays.asList(tag1, tag3)); // one new, one existing

        // then
        List<LastfmTag> allTags = repository.findAll();
        assertEquals(3, allTags.size(), "Second save of the same tag should not produce duplicates");
    }

}