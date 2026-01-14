package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl.LastfmTagServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
        
        EntityTagDto dto1 = new EntityTagDto(
            1L, 
            "rock", 
            ApprovalStatus.PENDING.getCode(),
            ApprovalStatus.PENDING.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            50
        );
        
        EntityTagDto dto2 = new EntityTagDto(
            2L, 
            "pop", 
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            100
        );
        
        List<EntityTagDto> dtos = Arrays.asList(dto1, dto2);
        
        when(tagRepository.findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            isNull(), 
            isNull(),
            isNull()
        )).thenReturn(dtos);

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals(1L, result.get(0).id());
        assertEquals("rock", result.get(0).name());
        assertEquals(ApprovalStatus.PENDING.getCode(), result.get(0).relationApprovalStatus());
        assertEquals(ApprovalStatus.PENDING.getCode(), result.get(0).tagApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(0).entityApprovalStatus());
        assertEquals(50, result.get(0).usageCount());
        
        assertEquals(2L, result.get(1).id());
        assertEquals("pop", result.get(1).name());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(1).relationApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(1).tagApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(1).entityApprovalStatus());
        assertEquals(100, result.get(1).usageCount());
        
        verify(tagRepository).findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            isNull(), 
            isNull(),
            isNull()
        );
    }

    @Test
    void findAllByEntity_shouldReturnEmptyListWhenNoTagsFound() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        
        when(tagRepository.findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            isNull(), 
            isNull(),
            isNull()
        )).thenReturn(Collections.emptyList());

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(tagRepository).findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            isNull(), 
            isNull(),
            isNull()
        );
    }

    @Test
    void findAllByEntity_shouldHandleDifferentEntityTypes() {
        // Given
        Long entityId = 456L;
        LastfmEntityType entityType = LastfmEntityType.TRACK;
        
        EntityTagDto dto = new EntityTagDto(
            3L, 
            "electronic", 
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.APPROVED.getCode(),
            ApprovalStatus.PENDING.getCode(),
            75
        );
        
        when(tagRepository.findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            isNull(), 
            isNull(),
            isNull()
        )).thenReturn(List.of(dto));

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).id());
        assertEquals("electronic", result.get(0).name());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(0).tagApprovalStatus());
        assertEquals(ApprovalStatus.PENDING.getCode(), result.get(0).entityApprovalStatus());
        assertEquals(75, result.get(0).usageCount());
        
        verify(tagRepository).findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            isNull(), 
            isNull(),
            isNull()
        );
    }
    
    @Test
    void findAllByEntity_shouldPassSearchParamsAndPageable() {
        // Given
        Long entityId = 123L;
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        Integer minUsageCount = 50;
        EntityTagSearchParams searchParams = new EntityTagSearchParams(minUsageCount, null);
        Pageable pageable = mock(Pageable.class);
        
        // Use specific values for non-null parameters and isNull() for null parameters
        when(tagRepository.findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            eq(minUsageCount), 
            isNull(), 
            eq(pageable)
        )).thenReturn(Collections.emptyList());

        // When
        List<EntityTagDto> result = tagService.findAllByEntity(entityType, entityId, searchParams, pageable);

        // Then
        assertNotNull(result);
        
        verify(tagRepository).findTagsByEntityWithFilters(
            eq(entityType), 
            eq(entityId), 
            eq(minUsageCount), 
            isNull(), 
            eq(pageable)
        );
    }
}
