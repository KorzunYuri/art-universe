package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyArtistService;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.lookup.SpotifyArtistLookupService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spotify/artists")
public class SpotifyArtistController {

    private final SpotifyArtistService artistService;
    private final SpotifyArtistLookupService artistLookupService;

    public SpotifyArtistController(SpotifyArtistService artistService,
                                   SpotifyArtistLookupService artistLookupService) {
        this.artistService = artistService;
        this.artistLookupService = artistLookupService;
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

    @GetMapping(value = "/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LookupResultDTO> lookup(
            @RequestParam String search,
            @RequestParam(required = false) Integer limit) {
        return artistLookupService.lookup(
                LookupRequestDTO.builder().search(search).limit(limit).build());
    }
}
