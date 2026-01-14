package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.DataQualityUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LastfmTrackServiceImpl implements LastfmTrackService {

    private final LastfmTrackRepository trackRepository;

    public LastfmTrackServiceImpl(LastfmTrackRepository trackRepository) {
        this.trackRepository = trackRepository;
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
                    result.put(trackDto, tracks.getFirst());
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
