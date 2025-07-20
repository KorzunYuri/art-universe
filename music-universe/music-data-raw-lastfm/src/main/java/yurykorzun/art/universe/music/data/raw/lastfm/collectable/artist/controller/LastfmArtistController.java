package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/artists")
@Slf4j
public class LastfmArtistController {

    private final LastfmArtistService artistService;

    public LastfmArtistController(LastfmArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseWrapper<Page<LastfmArtistResponseDto>>> getArtists(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long minPlayCount,
        @RequestParam(required = false) Long minListenersCount,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses);
            Page<LastfmArtistResponseDto> page = artistService.findAll(params, pageable);
            return ResponseWrapper.success(page);
        } catch (Exception e) {
            log.error("Failed to fetch artists: {}", e.getMessage(), e);
            return ResponseWrapper.failure("Failed to fetch artists: service error occurred");
        }
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseWrapper<LastfmArtistResponseDto>> getArtistById(@PathVariable Long id) {
        try {
            return artistService.findById(id)
                .map(artist -> ResponseWrapper.success(LastfmArtistResponseDto.from(artist)))
                .orElse(ResponseWrapper.failure("Artist not found with id: " + id, HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Failed to fetch artist with id {}: {}", id, e.getMessage(), e);
            return ResponseWrapper.failure("Failed to fetch artist: service error occurred");
        }
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ResponseWrapper<LastfmArtistResponseDto>> updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        try {
            LastfmArtistResponseDto artist = artistService.updateApprovalStatus(id, request.approvalStatus());
            return ResponseWrapper.success(artist);
        } catch (Exception e) {
            log.error("Failed to update approval status: {}", e.getMessage(), e);
            return ResponseWrapper.failure("Failed to update approval status: service error occurred");
        }
    }
}
