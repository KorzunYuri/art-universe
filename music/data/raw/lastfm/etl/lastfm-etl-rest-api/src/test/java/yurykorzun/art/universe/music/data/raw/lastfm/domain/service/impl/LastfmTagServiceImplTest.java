package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
