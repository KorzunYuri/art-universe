package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.entity.Track;
import yurykorzun.art.universe.music.quiz.repository.TrackRepository;
import yurykorzun.art.universe.music.quiz.service.TrackService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;

    @Override
    @Transactional
    public BindingDto bind(Long masterId) {
        log.debug("Binding track with masterId: {}", masterId);
        
        Optional<Track> existingTrack = trackRepository.findByMasterId(masterId);
        if (existingTrack.isPresent()) {
            log.debug("Track with masterId {} already bound", masterId);
            return createBindingDto(masterId, existingTrack.get());
        }

        try {
            Track track = new Track();
            track.setMasterId(masterId);
            Track savedTrack = trackRepository.save(track);
            
            log.debug("Successfully bound track with masterId: {}, bindingId: {}", masterId, savedTrack.getId());
            return createBindingDto(masterId, savedTrack);
        } catch (DataIntegrityViolationException e) {
            // Handle race condition - another thread might have inserted the same masterId
            log.debug("Race condition detected for masterId {}, checking existing binding", masterId);
            Optional<Track> raceConditionTrack = trackRepository.findByMasterId(masterId);
            if (raceConditionTrack.isPresent()) {
                return createBindingDto(masterId, raceConditionTrack.get());
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public BindingDto unbind(Long masterId) {
        log.debug("Unbinding track with masterId: {}", masterId);
        
        Optional<Track> existingTrack = trackRepository.findByMasterId(masterId);
        if (existingTrack.isEmpty()) {
            log.debug("Track with masterId {} not bound", masterId);
            return createBindingDto(masterId, null);
        }

        trackRepository.deleteByMasterId(masterId);
        log.debug("Successfully unbound track with masterId: {}", masterId);
        
        return createBindingDto(masterId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BindingDto getBinding(Long masterId) {
        log.debug("Getting binding for track with masterId: {}", masterId);
        
        Optional<Track> track = trackRepository.findByMasterId(masterId);
        return createBindingDto(masterId, track.orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BindingDto> getBindings(List<Long> masterIds) {
        log.debug("Getting bindings for {} track masterIds", masterIds.size());
        
        List<Track> boundTracks = trackRepository.findByMasterIdIn(masterIds);
        Map<Long, Track> boundTrackMap = boundTracks.stream()
            .collect(Collectors.toMap(Track::getMasterId, track -> track));

        return masterIds.stream()
            .map(masterId -> createBindingDto(masterId, boundTrackMap.get(masterId)))
            .collect(Collectors.toList());
    }

    /**
     * Creates a BindingDto from masterId and nullable Track entity
     */
    private BindingDto createBindingDto(Long masterId, @Nullable Track track) {
        return BindingDto.builder()
            .masterId(masterId)
            .isBound(track != null)
            .bindingId(track != null ? track.getId() : null)
            .build();
    }
}
