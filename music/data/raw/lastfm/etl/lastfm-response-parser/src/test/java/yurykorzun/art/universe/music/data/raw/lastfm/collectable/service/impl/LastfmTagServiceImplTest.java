package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTagServiceImplTest {
    
    @Mock
    private LastfmTagRepository tagRepository;

    @InjectMocks
    private LastfmTagServiceImpl tagService;

    private LastfmTag createTag() {
        return EntityCreationHelper.createTag();
    }

    private LastfmTag createTag(Consumer<LastfmTag.LastfmTagBuilder<?,?>> customizer) {
        return EntityCreationHelper.createTag(customizer);
    }

    @Test
    void findById_shouldReturnTagWhenExists() {
        // Given
        long tagId = 42L;
        LastfmTag expectedTag = createTag(b -> b.id(tagId));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(expectedTag));

        // When
        Optional<LastfmTag> result = tagService.findById(tagId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedTag, result.get());
        verify(tagRepository).findById(tagId);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenTagDoesNotExist() {
        // Given
        long tagId = 999L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        // When
        Optional<LastfmTag> result = tagService.findById(tagId);

        // Then
        assertFalse(result.isPresent());
        verify(tagRepository).findById(tagId);
    }

    @Test
    void saveAll_withValidTags_shouldCallRepository() {
        List<LastfmTag> tags = List.of(createTag(), createTag());
        when(tagRepository.saveAll(tags)).thenReturn(tags);

        List<LastfmTag> savedTags = tagService.saveAll(tags);

        assertNotNull(savedTags);
        assertEquals(tags.size(), savedTags.size());
        assertEquals(tags, savedTags);
        verify(tagRepository, times(1)).saveAll(tags);
    }
}
