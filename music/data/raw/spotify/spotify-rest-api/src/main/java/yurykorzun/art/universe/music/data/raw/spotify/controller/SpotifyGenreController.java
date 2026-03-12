package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyGenreResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyGenre;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyGenreRepository;

@RestController
@RequestMapping("/api/v1/spotify/genres")
public class SpotifyGenreController {

    private final SpotifyGenreRepository genreRepository;

    public SpotifyGenreController(SpotifyGenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<SpotifyGenreResponseDto> getGenres(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return genreRepository.findAll(pageable).map(SpotifyGenreResponseDto::from);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpotifyGenreResponseDto getGenreById(@PathVariable Long id) {
        SpotifyGenre genre = genreRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Genre not found with id: " + id));
        return SpotifyGenreResponseDto.from(genre);
    }
}
