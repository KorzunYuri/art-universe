package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyTrackRepository;

@RestController
@RequestMapping("/api/v1/spotify/tracks")
public class SpotifyTrackController {

    private final SpotifyTrackRepository trackRepository;

    public SpotifyTrackController(SpotifyTrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SpotifyTrackResponseDto> getTracks(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return trackRepository.findTracks(search, pageable).map(SpotifyTrackResponseDto::from);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpotifyTrackResponseDto getTrackById(@PathVariable Long id) {
        SpotifyTrack track = trackRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Track not found with id: " + id));
        return SpotifyTrackResponseDto.from(track);
    }
}
