package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/albums")
public class LastfmAlbumController {

    private final LastfmAlbumService albumService;

    public LastfmAlbumController(LastfmAlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<LastfmAlbumResponseDto> getAlbums(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long minPlayCount,
        @RequestParam(required = false) Long minListenersCount,
        @RequestParam(required = false) Long artistId,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @RequestParam(required = false) Long tagId,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        AlbumSearchParams params = new AlbumSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId);
        return albumService.findAll(params, pageable);
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmAlbumResponseDto getAlbumById(@PathVariable Long id) {
        return albumService.findById(id);
    }

    @GetMapping(
        value = "/{id}/tracks",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<LastfmAlbumTrackResponseDto> getAlbumTracks(@PathVariable Long id) {
        return albumService.findAlbumTracks(id);
    }
}
