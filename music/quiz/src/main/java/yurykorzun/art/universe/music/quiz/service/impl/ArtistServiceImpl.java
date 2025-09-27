package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.entity.Artist;
import yurykorzun.art.universe.music.quiz.repository.ArtistRepository;
import yurykorzun.art.universe.music.quiz.service.ArtistService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;

    @Override
    @Transactional
    public BindingDto bind(Long masterId) {
        log.debug("Binding artist with masterId: {}", masterId);
        
        Optional<Artist> existingArtist = artistRepository.findByMasterId(masterId);
        if (existingArtist.isPresent()) {
            log.debug("Artist with masterId {} already bound", masterId);
            return createBindingDto(masterId, existingArtist.get());
        }

        try {
            Artist artist = new Artist();
            artist.setMasterId(masterId);
            Artist savedArtist = artistRepository.save(artist);
            
            log.debug("Successfully bound artist with masterId: {}, bindingId: {}", masterId, savedArtist.getId());
            return createBindingDto(masterId, savedArtist);
        } catch (DataIntegrityViolationException e) {
            // Handle race condition - another thread might have inserted the same masterId
            log.debug("Race condition detected for masterId {}, checking existing binding", masterId);
            Optional<Artist> raceConditionArtist = artistRepository.findByMasterId(masterId);
            if (raceConditionArtist.isPresent()) {
                return createBindingDto(masterId, raceConditionArtist.get());
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public BindingDto unbind(Long masterId) {
        log.debug("Unbinding artist with masterId: {}", masterId);
        
        Optional<Artist> existingArtist = artistRepository.findByMasterId(masterId);
        if (existingArtist.isEmpty()) {
            log.debug("Artist with masterId {} not bound", masterId);
            return createBindingDto(masterId, null);
        }

        artistRepository.deleteByMasterId(masterId);
        log.debug("Successfully unbound artist with masterId: {}", masterId);
        
        return createBindingDto(masterId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BindingDto getBinding(Long masterId) {
        log.debug("Getting binding for artist with masterId: {}", masterId);
        
        Optional<Artist> artist = artistRepository.findByMasterId(masterId);
        return createBindingDto(masterId, artist.orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BindingDto> getBindings(List<Long> masterIds) {
        log.debug("Getting bindings for {} artist masterIds", masterIds.size());
        
        List<Artist> boundArtists = artistRepository.findByMasterIdIn(masterIds);
        Map<Long, Artist> boundArtistMap = boundArtists.stream()
            .collect(Collectors.toMap(Artist::getMasterId, artist -> artist));

        return masterIds.stream()
            .map(masterId -> createBindingDto(masterId, boundArtistMap.get(masterId)))
            .collect(Collectors.toList());
    }

    /**
     * Creates a BindingDto from masterId and nullable Artist entity
     */
    private BindingDto createBindingDto(Long masterId, @Nullable Artist artist) {
        return BindingDto.builder()
            .masterId(masterId)
            .isBound(artist != null)
            .bindingId(artist != null ? artist.getId() : null)
            .build();
    }
}
