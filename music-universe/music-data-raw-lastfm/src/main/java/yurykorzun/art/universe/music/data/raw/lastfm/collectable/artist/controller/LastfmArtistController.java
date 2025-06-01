package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;

@RestController
@RequestMapping("/api/v1/artists")
public class LastfmArtistController {

    private final LastfmArtistService artistService;

    public LastfmArtistController(LastfmArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseWrapper<Page<LastfmArtistDto>>> getArtists(
        @RequestParam(defaultValue = "") String search,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            Page<LastfmArtist> page = artistService.findByName(search, pageable);
            Page<LastfmArtistDto> dtoPage = page.map(LastfmArtistDto::from);
            return ResponseWrapper.success(dtoPage);
        } catch (Exception e) {
            return ResponseWrapper.failure("Failed to fetch artists: " + e.getMessage());
        }
    }

}
