package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;

import java.util.List;

@Service
public class LastfmTagServiceImpl implements LastfmTagService {

    private final LastfmTagRepository tagRepository;

    public LastfmTagServiceImpl(LastfmTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public LastfmTagResponseDto findById(Long id) {
        return tagRepository.findById(id)
            .map(LastfmTagResponseDto::from)
            .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
    }

    @Override
    public Page<LastfmTagResponseDto> findAll(TagSearchParams params, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(params);
        Page<LastfmTag> tagsPage = tagRepository.findTags(
            params.search(),
            approvalStatuses,
            params.minUsageCount(),
            params.minUsageUsersCount(),
            pageable);
        return tagsPage.map(LastfmTagResponseDto::from);
    }
    
    private static List<ApprovalStatus> getApprovalStatusesFromCodes(TagSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
    
    @Override
    public List<EntityTagDto> findAllByEntity(LastfmEntityType entityType, Long entityId, 
                                             EntityTagSearchParams searchParams, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = null;
        if (searchParams != null && searchParams.approvalStatuses() != null) {
            approvalStatuses = CodedRegistry.getByCodes(searchParams.approvalStatuses(), ApprovalStatus.class);
        }
        
        // Repository now returns EntityTagDto directly
        return tagRepository.findTagsByEntityWithFilters(
            entityType, 
            entityId, 
            searchParams != null ? searchParams.minUsageCount() : null,
            approvalStatuses,
            pageable
        );
    }
}
