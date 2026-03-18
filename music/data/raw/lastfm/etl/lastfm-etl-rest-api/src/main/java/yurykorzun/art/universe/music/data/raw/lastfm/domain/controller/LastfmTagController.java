package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;

@RestController
@RequestMapping("/api/v1/tags")
public class LastfmTagController {

    private final LastfmTagService tagService;

    public LastfmTagController(LastfmTagService tagService) {
        this.tagService = tagService;
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('LASTFM_CURATOR')")
    public LastfmTagResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        return tagService.updateApprovalStatus(id, request.approvalStatus());
    }
}
