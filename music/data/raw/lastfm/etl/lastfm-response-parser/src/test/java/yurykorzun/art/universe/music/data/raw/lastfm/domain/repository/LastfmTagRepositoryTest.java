package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmTagRepository tagRepository;

    @Test
    void findAllByNameIn_shouldReturnMatchingTags() {
        // Given
        LastfmTag tag1 = consistencyHelper.createAndSaveTag(builder -> builder.name("rock"));
        LastfmTag tag2 = consistencyHelper.createAndSaveTag(builder -> builder.name("pop"));
        LastfmTag tag3 = consistencyHelper.createAndSaveTag(builder -> builder.name("jazz"));
        
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
