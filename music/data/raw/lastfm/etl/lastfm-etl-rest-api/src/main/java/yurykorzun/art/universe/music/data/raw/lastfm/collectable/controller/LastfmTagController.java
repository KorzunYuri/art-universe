package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;

@RestController
@RequestMapping("/api/v1/tags")
public class LastfmTagController {

    private final LastfmTagService tagService;

    public LastfmTagController(LastfmTagService tagService) {
        this.tagService = tagService;
    }

    @PatchMapping("/{id}/approval")
    public LastfmTagResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        return tagService.updateApprovalStatus(id, request.approvalStatus());
    }
}
