package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyAlbumService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spotify/albums")
public class SpotifyAlbumController {

    private final SpotifyAlbumService albumService;

    public SpotifyAlbumController(SpotifyAlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SpotifyAlbumResponseDto> getAlbums(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return albumService.findAll(search, pageable);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpotifyAlbumResponseDto getAlbumById(@PathVariable Long id) {
        return albumService.findById(id);
    }

    @GetMapping(value = "/{id}/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SpotifyAlbumTrackResponseDto> getAlbumTracks(@PathVariable Long id) {
        return albumService.findAlbumTracks(id);
    }
}
