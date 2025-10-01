package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmAlbumService;

@RestController
@RequestMapping("/api/v1/albums")
public class LastfmAlbumController {

    private final LastfmAlbumService albumService;

    public LastfmAlbumController(LastfmAlbumService albumService) {
        this.albumService = albumService;
    }

    @PatchMapping("/{id}/approval")
    public LastfmAlbumResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        return albumService.updateApprovalStatus(id, request.approvalStatus());
    }
}
