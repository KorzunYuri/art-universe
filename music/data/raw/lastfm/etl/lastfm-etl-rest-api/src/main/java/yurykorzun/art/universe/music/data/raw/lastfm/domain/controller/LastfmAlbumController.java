package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

@RestController
@RequestMapping("/api/v1/albums")
public class LastfmAlbumController {

    private final LastfmAlbumService albumService;

    public LastfmAlbumController(LastfmAlbumService albumService) {
        this.albumService = albumService;
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('LASTFM_CURATOR')")
    public LastfmAlbumResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        return albumService.updateApprovalStatus(id, request.approvalStatus());
    }
}
