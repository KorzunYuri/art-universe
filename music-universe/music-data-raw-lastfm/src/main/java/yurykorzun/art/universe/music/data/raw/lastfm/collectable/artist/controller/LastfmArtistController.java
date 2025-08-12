package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.common.exception.DataFetchException;
import yurykorzun.art.universe.common.exception.DataUpdateException;

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
    public Page<LastfmArtistResponseDto> getArtists(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long minPlayCount,
        @RequestParam(required = false) Long minListenersCount,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses);
            return artistService.findAll(params, pageable);
        } catch (Exception e) {
            log.error("Failed to fetch artists: {}", e.getMessage(), e);
            throw new DataFetchException("Failed to fetch artists: service error occurred", e);
        }
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmArtistResponseDto getArtistById(@PathVariable Long id) {
        try {
            return artistService.findById(id)
                .map(LastfmArtistResponseDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Artist", id));
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch artist with id {}: {}", id, e.getMessage(), e);
            throw new DataFetchException("Failed to fetch artist: service error occurred", e);
        }
    }

    @PatchMapping("/{id}/approval")
    public LastfmArtistResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        try {
            return artistService.updateApprovalStatus(id, request.approvalStatus());
        } catch (Exception e) {
            log.error("Failed to update approval status: {}", e.getMessage(), e);
            throw new DataUpdateException("Failed to update approval status: service error occurred", e);
        }
    }
}
