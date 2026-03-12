package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyArtistService;

@RestController
@RequestMapping("/api/v1/spotify/artists")
public class SpotifyArtistController {

    private final SpotifyArtistService artistService;

    public SpotifyArtistController(SpotifyArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SpotifyArtistResponseDto> getArtists(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return artistService.findAll(search, pageable);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpotifyArtistResponseDto getArtistById(@PathVariable Long id) {
        return artistService.findById(id);
    }
}
