package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;

@RestController
@RequestMapping("/api/v1/tracks")
public class LastfmTrackController {

    private final LastfmTrackService trackService;

    public LastfmTrackController(LastfmTrackService trackService) {
        this.trackService = trackService;
    }

    @PatchMapping(
        value = "/{id}/approval",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmTrackResponseDto updateApprovalStatus(
            @PathVariable Long id,
            @RequestBody ApprovalStatusRequestDto request
    ) {
        return trackService.updateApprovalStatus(id, request.approvalStatus());
    }
}
