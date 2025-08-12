package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;
import yurykorzun.art.universe.common.exception.DataFetchException;
import yurykorzun.art.universe.common.exception.DataUpdateException;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/tracks")
@Slf4j
public class LastfmTrackController {

    private final LastfmTrackService trackService;

    public LastfmTrackController(LastfmTrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<LastfmTrackResponseDto> getTracks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long minPlayCount,
            @RequestParam(required = false) Long minListenersCount,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Set<Integer> approvalStatuses,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            TrackSearchParams params = new TrackSearchParams(
                    search,
                    minPlayCount,
                    minListenersCount,
                    artistId,
                    approvalStatuses
            );
            
            return trackService.findAll(params, pageable);
        } catch (Exception e) {
            log.error("Failed to fetch tracks: {}", e.getMessage(), e);
            throw new DataFetchException("Failed to fetch tracks: service error occurred", e);
        }
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmTrackResponseDto getTrackById(@PathVariable Long id) {
        try {
            return trackService.findById(id)
                .map(LastfmTrackResponseDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Track", id));
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch track with id {}: {}", id, e.getMessage(), e);
            throw new DataFetchException("Failed to fetch track: service error occurred", e);
        }
    }

    @PatchMapping(
        value = "/{id}/approval",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmTrackResponseDto updateApprovalStatus(
            @PathVariable Long id,
            @RequestBody ApprovalStatusRequestDto request
    ) {
        try {
            return trackService.updateApprovalStatus(id, request.approvalStatus());
        } catch (Exception e) {
            log.error("Failed to update approval status for track {}: {}", id, e.getMessage(), e);
            throw new DataUpdateException("Failed to update approval status: service error occurred", e);
        }
    }
}
