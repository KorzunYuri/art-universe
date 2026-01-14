package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.TrackSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/tracks")
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
            @RequestParam(required = false) Long tagId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        TrackSearchParams params = new TrackSearchParams(
                search,
                minPlayCount,
                minListenersCount,
                artistId,
                approvalStatuses,
                tagId
        );
        
        return trackService.findAll(params, pageable);
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmTrackResponseDto getTrackById(@PathVariable Long id) {
        return trackService.findById(id);
    }
}
