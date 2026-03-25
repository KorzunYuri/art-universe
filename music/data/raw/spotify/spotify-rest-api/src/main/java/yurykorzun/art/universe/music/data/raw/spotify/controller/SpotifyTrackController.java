package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyTrackService;

@RestController
@RequestMapping("/api/v1/spotify/tracks")
public class SpotifyTrackController {

    private final SpotifyTrackService trackService;

    public SpotifyTrackController(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SpotifyTrackResponseDto> getTracks(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return trackService.findAll(search, pageable);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpotifyTrackResponseDto getTrackById(@PathVariable Long id) {
        return trackService.findById(id);
    }
}
