package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;

import java.util.*;

@Service
public class LastfmTrackServiceImpl implements LastfmTrackService {

    private final LastfmTrackRepository trackRepository;

    public LastfmTrackServiceImpl(LastfmTrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public LastfmTrackResponseDto findById(Long id) {
        return trackRepository.findById(id)
            .map(LastfmTrackResponseDto::from)
            .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + id));
    }

    @Override
    public Page<LastfmTrackResponseDto> findAll(TrackSearchParams params, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(params);
        Page<LastfmTrack> tracksPage = trackRepository.findTracks(
                params.search(),
                params.minPlayCount(),
                params.minListenersCount(),
                params.artistId(),
                approvalStatuses,
                params.tagId(),
                pageable);
        return tracksPage.map(LastfmTrackResponseDto::from);
    }
    
    private static List<ApprovalStatus> getApprovalStatusesFromCodes(TrackSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
