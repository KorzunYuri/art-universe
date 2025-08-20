package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LastfmTagServiceImpl implements LastfmTagService {

    private final LastfmTagRepository tagRepository;

    public LastfmTagServiceImpl(LastfmTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<LastfmTag> findAllByNameIn(List<String> tagNames) {
        return tagRepository.findAllByNameIn(tagNames);
    }

    @Override
    public List<LastfmTag> saveAll(List<LastfmTag> tags) {
        return tagRepository.saveAll(tags);
    }

    @Override
    public <D extends EntityDto<LastfmTag>> Map<D, LastfmTag> mapDtoToExistingEntities(List<D> dtos) {
        Map<D, LastfmTag> result = new HashMap<>();

        Map<String, D> nameToDto = new HashMap<>(); // helper map
        List<String> names = dtos.stream()
            .peek(dto -> nameToDto.put(dto.getName(), dto))
            .peek(dto -> result.put(dto, null))
            .map(EntityDto::getName)
            .toList();

        List<LastfmTag> existingTags = tagRepository.findAllByNameIn(names);
        existingTags.forEach(tag -> result.put(nameToDto.get(tag.getName()), tag));

        return result;
    }

    @Override
    public Optional<LastfmTag> findById(Long id) {
        return tagRepository.findById(id);
    }

    @Override
    public LastfmTagResponseDto findDtoById(Long id) {
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
    public LastfmTagResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        
        LastfmTag tag = tagRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        
        tag.updateApprovalStatus(approvalStatus);
        LastfmTag updated = tagRepository.save(tag);
        return LastfmTagResponseDto.from(updated);
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
