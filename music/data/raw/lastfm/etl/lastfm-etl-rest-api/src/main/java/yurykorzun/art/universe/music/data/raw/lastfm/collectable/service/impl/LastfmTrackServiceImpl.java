package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTrackService;

@Service
public class LastfmTrackServiceImpl implements LastfmTrackService {

    private final LastfmTrackRepository trackRepository;

    public LastfmTrackServiceImpl(LastfmTrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        
        LastfmTrack track = trackRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + id));
        
        track.updateApprovalStatus(approvalStatus);
        LastfmTrack updated = trackRepository.save(track);
        return LastfmTrackResponseDto.from(updated);
    }
}
