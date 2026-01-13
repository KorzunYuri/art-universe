package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.lookup.LastfmArtistLookupService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/artists")
public class LastfmArtistController {

    private final LastfmArtistService artistService;
    private final LastfmArtistLookupService artistLookupService;

    public LastfmArtistController(LastfmArtistService artistService, LastfmArtistLookupService artistLookupService) {
        this.artistService = artistService;
        this.artistLookupService = artistLookupService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<LastfmArtistResponseDto> getArtists(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long minPlayCount,
        @RequestParam(required = false) Long minListenersCount,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @RequestParam(required = false) Long tagId,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses, tagId);
        return artistService.findAll(params, pageable);
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmArtistResponseDto getArtistById(@PathVariable Long id) {
        return artistService.findById(id);
    }

    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupArtists(
        @RequestParam String search,
        @RequestParam(required = false) Integer limit
    ) {
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();
        return artistLookupService.lookup(request);
    }
}
