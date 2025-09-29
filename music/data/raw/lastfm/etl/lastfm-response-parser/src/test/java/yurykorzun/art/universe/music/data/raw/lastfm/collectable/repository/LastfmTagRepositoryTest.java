package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void findAllByNameIn_shouldReturnMatchingTags() {
        // Given
        LastfmTag tag1 = dbHelper.createAndSaveTag(builder -> builder.name("rock"));
        LastfmTag tag2 = dbHelper.createAndSaveTag(builder -> builder.name("pop"));
        LastfmTag tag3 = dbHelper.createAndSaveTag(builder -> builder.name("jazz"));
        
        // When
        List<LastfmTag> result = tagRepository.findAllByNameIn(List.of("rock", "jazz"));
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tag -> tag.getId() == tag1.getId()));
        assertTrue(result.stream().anyMatch(tag -> tag.getId() == tag3.getId()));
        assertFalse(result.stream().anyMatch(tag -> tag.getId() == tag2.getId()));
    }
}
