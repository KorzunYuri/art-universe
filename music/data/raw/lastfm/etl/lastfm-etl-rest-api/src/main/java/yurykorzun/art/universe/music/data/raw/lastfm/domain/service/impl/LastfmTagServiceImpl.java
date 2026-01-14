package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;

@Service
public class LastfmTagServiceImpl implements LastfmTagService {

    private final LastfmTagRepository tagRepository;

    public LastfmTagServiceImpl(LastfmTagRepository tagRepository) {
        this.tagRepository = tagRepository;
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
}
