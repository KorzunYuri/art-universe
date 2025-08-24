package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.DataQualityUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTrackService;

import java.util.*;
import java.util.stream.Collectors;

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
    public Optional<LastfmTrack> findById(Long id) {
        return trackRepository.findById(id);
    }

    @Override
    public LastfmTrackResponseDto findDtoById(Long id) {
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
    
    @Override
    public List<LastfmTrack> findTracksForGetInfo() {
        return trackRepository.findTracksForGetInfo();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends EntityDto<LastfmTrack>> Map<D, LastfmTrack> mapDtoToExistingEntities(List<D> dtos) {
        Map<TrackDto, LastfmTrack> result = new HashMap<>();
        
        // Step 1: Try to find tracks by name + artist (individual queries)
        List<TrackDto> unmatchedDtos = new ArrayList<>();
        for (D dto : dtos) {
            TrackDto trackDto = (TrackDto) dto;
            
            if (trackDto.getArtistName() != null) {
                List<LastfmTrack> tracks = trackRepository.findByNameAndArtistName(trackDto.getName(), trackDto.getArtistName());
                if (tracks.size() == 1) {
                    result.put(trackDto, tracks.get(0));
                    continue;
                } else if (tracks.size() > 1) {
                    throw new IllegalStateException(String.format("Multiple tracks found for track %s - %s with url %s and mbid %s",
                        trackDto.getArtistName(), trackDto.getName(), trackDto.getUrl(), trackDto.getMbid()));
                }
            }
            unmatchedDtos.add(trackDto);
        }
        
        // Step 2: Batch search by URLs for remaining DTOs
        if (!unmatchedDtos.isEmpty()) {
            List<String> urls = unmatchedDtos.stream()
                .filter(dto -> dto.getUrl() != null)
                .map(dto -> DataQualityUtil.normalizeTrackUrl(dto.getUrl()))
                .distinct()
                .collect(Collectors.toList());
                
            if (!urls.isEmpty()) {
                Map<String, LastfmTrack> tracksByUrl = trackRepository.findAllByUrlIn(urls).stream()
                    .collect(Collectors.toMap(
                        track -> DataQualityUtil.normalizeTrackUrl(track.getUrl()),
                        track -> track,
                        (existing, replacement) -> {
                            throw new IllegalStateException("Multiple tracks found for URL: " + existing.getUrl());
                        }
                    ));
                
                // Match DTOs with found tracks by URL
                Iterator<TrackDto> iterator = unmatchedDtos.iterator();
                while (iterator.hasNext()) {
                    TrackDto dto = iterator.next();
                    if (dto.getUrl() != null) {
                        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(dto.getUrl());
                        LastfmTrack track = tracksByUrl.get(normalizedUrl);
                        if (track != null) {
                            result.put(dto, track);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        
        // Step 3: Batch search by MBIDs for remaining DTOs
        if (!unmatchedDtos.isEmpty()) {
            List<String> mbids = unmatchedDtos.stream()
                .filter(dto -> dto.getMbid() != null && !dto.getMbid().isEmpty())
                .map(TrackDto::getMbid)
                .distinct()
                .collect(Collectors.toList());
                
            if (!mbids.isEmpty()) {
                Map<String, LastfmTrack> tracksByMbid = trackRepository.findAllByMbidIn(mbids).stream()
                    .collect(Collectors.toMap(
                        LastfmTrack::getMbid,
                        track -> track,
                        (existing, replacement) -> {
                            throw new IllegalStateException("Multiple tracks found for MBID: " + existing.getMbid());
                        }
                    ));
                
                // Match DTOs with found tracks by MBID
                for (TrackDto dto : unmatchedDtos) {
                    if (dto.getMbid() != null && !dto.getMbid().isEmpty()) {
                        LastfmTrack track = tracksByMbid.get(dto.getMbid());
                        if (track != null) {
                            result.put(dto, track);
                        }
                    }
                }
            }
        }
        
        // Add null entries for unmatched DTOs
        for (D dto : dtos) {
            TrackDto trackDto = (TrackDto) dto;
            if (!result.containsKey(trackDto)) {
                result.put(trackDto, null);
            }
        }

        return (Map<D, LastfmTrack>) result;
    }
}
