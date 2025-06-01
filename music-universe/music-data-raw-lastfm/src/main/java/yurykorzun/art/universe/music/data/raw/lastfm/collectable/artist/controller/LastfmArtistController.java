package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;

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
    public ResponseEntity<ResponseWrapper<Page<LastfmArtistResponseDto>>> getArtists(
        @RequestParam(defaultValue = "") String search,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            Page<LastfmArtist> page = artistService.findByName(search, pageable);
            Page<LastfmArtistResponseDto> dtoPage = page.map(LastfmArtistResponseDto::from);
            return ResponseWrapper.success(dtoPage);
        } catch (Exception e) {
            return ResponseWrapper.failure("Failed to fetch artists: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ResponseWrapper<LastfmArtistResponseDto>> updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        try {
            LastfmArtist artist = artistService.updateApprovalStatus(id, request.approvalStatus());
            return ResponseWrapper.success(LastfmArtistResponseDto.from(artist));
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to update artist {%s} approval status: %s", id, e.getMessage()));
        }
    }
}
