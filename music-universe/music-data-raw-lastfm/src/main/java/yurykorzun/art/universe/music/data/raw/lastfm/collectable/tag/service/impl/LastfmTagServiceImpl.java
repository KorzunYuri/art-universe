package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.List;

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
    public List<LastfmTag> saveTags(List<LastfmTag> tags) {
        return tagRepository.saveAll(tags);
    }
    
    @Override
    public Page<LastfmTagResponseDto> findTags(TagSearchParams params, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(params);
        Page<LastfmTag> tagsPage = tagRepository.findTags(
            params.search(),
            approvalStatuses,
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
            .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        
        tag.updateApprovalStatus(approvalStatus);
        LastfmTag updated = tagRepository.save(tag);
        return LastfmTagResponseDto.from(updated);
    }
}
