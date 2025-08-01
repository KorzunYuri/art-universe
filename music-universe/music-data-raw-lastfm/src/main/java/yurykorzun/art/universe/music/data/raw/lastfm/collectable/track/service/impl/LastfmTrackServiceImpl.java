package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;
import java.util.Optional;

@Service
public class LastfmTrackServiceImpl implements LastfmTrackService {

    private final LastfmTrackRepository trackRepository;

    public LastfmTrackServiceImpl(LastfmTrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public LastfmTrack save(LastfmTrack lastfmTrack) {
        return trackRepository.save(lastfmTrack);
    }

    @Override
    public List<LastfmTrack> saveAll(List<LastfmTrack> lastfmTracks) {
        return trackRepository.saveAll(lastfmTracks);
    }

    @Override
    public List<LastfmTrack> findAllByUrls(List<String> urls) {
        return trackRepository.findAllByUrlIn(urls);
    }

    @Override
    public List<LastfmTrack> findEntitiesByUniqueKeys(List<String> uniqueKeys) {
        return findAllByUrls(uniqueKeys);
    }

    @Override
    public Optional<LastfmTrack> findById(Long id) {
        return trackRepository.findById(id);
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
                pageable);
        return tracksPage.map(LastfmTrackResponseDto::from);
    }
    
    private static List<ApprovalStatus> getApprovalStatusesFromCodes(TrackSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
    
    @Override
    public LastfmTrackResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        
        LastfmTrack track = trackRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        
        track.updateApprovalStatus(approvalStatus);
        LastfmTrack updated = trackRepository.save(track);
        return LastfmTrackResponseDto.from(updated);
    }
    
    @Override
    public List<LastfmTrack> findTracksForGetInfo() {
        return trackRepository.findTracksForGetInfo();
    }
}
