package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;

@RestController
@RequestMapping("/api/v1/artists")
public class LastfmArtistController {

    private final LastfmArtistService artistService;
    private final LastfmArtistSearchRequestService artistSearchRequestService;

    public LastfmArtistController(LastfmArtistService artistService, LastfmArtistSearchRequestService artistSearchRequestService) {
        this.artistService = artistService;
        this.artistSearchRequestService = artistSearchRequestService;
    }

    @PatchMapping("/{id}/approval")
    public LastfmArtistResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        return artistService.updateApprovalStatus(id, request.approvalStatus());
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.CREATED)
    public LastfmArtistSearchRequest createSearchRequest(@Valid @RequestBody ArtistSearchRequestDto request) {
        return artistSearchRequestService.saveRequest(request.searchString());
    }
}
