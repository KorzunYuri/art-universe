package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;

import java.util.Set;

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
    public Page<LastfmArtistResponseDto> getArtists(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long minPlayCount,
        @RequestParam(required = false) Long minListenersCount,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses);
        return artistService.findAll(params, pageable);
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmArtistResponseDto getArtistById(@PathVariable Long id) {
        return artistService.findDtoById(id);
    }

    @PatchMapping("/{id}/approval")
    public LastfmArtistResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        return artistService.updateApprovalStatus(id, request.approvalStatus());
    }
}
