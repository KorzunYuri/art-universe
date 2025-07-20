package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
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
    void saveAll_withValidAll_shouldCallRepository() {
        List<LastfmTag> tags = List.of(createTag(), createTag());
        when(tagRepository.saveAll(tags)).thenReturn(tags);

        List<LastfmTag> savedTags = tagService.saveAll(tags);

        assertNotNull(savedTags);
        assertEquals(tags.size(), savedTags.size());
        assertEquals(tags, savedTags);
        verify(tagRepository, times(1)).saveAll(tags);
    }

    @Test
    void findAllByNameIn_withValidNames_shouldCallRepository() {
        final int tagsNumber = 3;
        List<String> names = List.of("rock", "pop", "jazz");
        List<LastfmTag> tags = names.stream()
            .map(name -> createTag(builder -> builder.name(name)))
            .toList();
        when(tagRepository.findAllByNameIn(names)).thenReturn(tags);

        List<LastfmTag> foundTags = tagService.findAllByNameIn(names);

        assertNotNull(foundTags);
        assertEquals(tags.size(), foundTags.size());
        assertEquals(tags, foundTags);
        verify(tagRepository, times(1)).findAllByNameIn(names);
    }
    
    @Test
    void findAll_shouldCallRepositoryWithCorrectParams() {
        // Given
        String search = "test";
        Set<Integer> approvalStatusCodes = Set.of(ApprovalStatus.APPROVED.getCode());
        List<ApprovalStatus> approvalStatuses = CodedRegistry.getByCodes(approvalStatusCodes, ApprovalStatus.class);
        
        TagSearchParams params = new TagSearchParams(search, approvalStatusCodes);
        Pageable pageable = PageRequest.of(0, 10);
        
        List<LastfmTag> tags = List.of(createTag(), createTag());
        Page<LastfmTag> tagPage = new PageImpl<>(tags, pageable, tags.size());
        
        when(tagRepository.findTags(
            eq(search), 
            eq(approvalStatuses),
            eq(pageable)
        )).thenReturn(tagPage);
        
        // When
        Page<LastfmTagResponseDto> result = tagService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(tags.size(), result.getContent().size());
        verify(tagRepository).findTags(
            eq(search), 
            eq(approvalStatuses),
            eq(pageable)
        );
    }
    
    @Test
    void findAll_withNullParams_shouldCallRepositoryWithNullValues() {
        // Given
        TagSearchParams params = new TagSearchParams(null, null);
        Pageable pageable = PageRequest.of(0, 10);
        List<ApprovalStatus> expectedApprovalStatuses = Collections.emptyList();

        List<LastfmTag> tags = List.of(createTag(), createTag());
        Page<LastfmTag> tagPage = new PageImpl<>(tags, pageable, tags.size());
        
        when(tagRepository.findTags(
            eq(null), 
            eq(expectedApprovalStatuses),
            eq(pageable)
        )).thenReturn(tagPage);
        
        // When
        Page<LastfmTagResponseDto> result = tagService.findAll(params, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(tags.size(), result.getContent().size());
        verify(tagRepository).findTags(
            eq(null), 
            eq(expectedApprovalStatuses),
            eq(pageable)
        );
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedTag() {
        long tagId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmTag existing = createTag(b -> b.id(tagId).approvalStatus(oldStatus));
        LastfmTag updated = createTag(b -> b.id(tagId).approvalStatus(newStatus));

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existing));
        when(tagRepository.save(any(LastfmTag.class))).thenReturn(updated);

        LastfmTagResponseDto result = tagService.updateApprovalStatus(tagId, newStatus.getCode());

        assertEquals(newStatus.getCode(), result.approvalStatus());
        verify(tagRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingTag_shouldThrowException() {
        when(tagRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> tagService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> tagService.updateApprovalStatus(1L, -1)
        );
    }
}
