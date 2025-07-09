package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl.LastfmTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTagServiceTest {

    @Mock
    private LastfmTagRepository tagRepository;

    @InjectMocks
    private LastfmTagServiceImpl tagService;

    @Test
    void findAllByEntity_shouldReturnEntityTagDtos() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
        LastfmTag tag1 = EntityCreationHelper.createTag(builder -> builder
            .name("rock")
            .id(1L));
        LastfmTag tag2 = EntityCreationHelper.createTag(builder -> builder
            .name("pop")
            .id(2L));
        
        List<LastfmTag> tags = Arrays.asList(tag1, tag2);
        
        when(tagRepository.findTagsByEntity(entityType, entityId)).thenReturn(tags);

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals(1L, result.get(0).id());
        assertEquals("rock", result.get(0).name());
        
        assertEquals(2L, result.get(1).id());
        assertEquals("pop", result.get(1).name());
        
        verify(tagRepository).findTagsByEntity(entityType, entityId);
    }

    @Test
    void findAllByEntity_shouldReturnEmptyListWhenNoAllFound() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
        when(tagRepository.findTagsByEntity(entityType, entityId)).thenReturn(Collections.emptyList());

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(tagRepository).findTagsByEntity(entityType, entityId);
    }

    @Test
    void findAllByEntity_shouldHandleDifferentEntityTypes() {
        // Given
        Long entityId = 456L;
        LastfmEntityType entityType = LastfmEntityType.TRACK;
        
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder
            .name("electronic")
            .id(3L));
        
        when(tagRepository.findTagsByEntity(entityType, entityId)).thenReturn(List.of(tag));

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).id());
        assertEquals("electronic", result.get(0).name());
        
        verify(tagRepository).findTagsByEntity(entityType, entityId);
    }
}
