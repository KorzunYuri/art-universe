package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyAlbumRepository;

@RestController
@RequestMapping("/api/v1/spotify/albums")
public class SpotifyAlbumController {

    private final SpotifyAlbumRepository albumRepository;

    public SpotifyAlbumController(SpotifyAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SpotifyAlbumResponseDto> getAlbums(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return albumRepository.findAlbums(search, pageable).map(SpotifyAlbumResponseDto::from);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpotifyAlbumResponseDto getAlbumById(@PathVariable Long id) {
        SpotifyAlbum album = albumRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Album not found with id: " + id));
        return SpotifyAlbumResponseDto.from(album);
    }
}
