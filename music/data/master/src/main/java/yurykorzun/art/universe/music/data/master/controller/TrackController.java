package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.service.TrackService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;
    private final BindingService bindingService;

    public TrackController(TrackService trackService, BindingService bindingService) {
        this.trackService = trackService;
        this.bindingService = bindingService;
    }

    @GetMapping("/bound/{dataSource}")
    public List<BoundEntityProjection> findBoundTracks(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        return trackService.findBoundTracks(dataSource, externalIds);
    }
    
    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupTracks(
        @RequestParam String search,
        @RequestParam(required = false) DataSource dataSource,
        @RequestParam(required = false) Long masterArtistId,
        @RequestParam(required = false) Long externalArtistId,
        @RequestParam(required = false) Integer limit
    ) {
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search(search)
            .dataSource(dataSource)
            .masterArtistId(masterArtistId)
            .externalArtistId(externalArtistId)
            .limit(limit)
            .build();
        return trackService.lookupTracks(request);
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupTracks(
        @Valid @RequestBody ArtistRelatedBatchLookupRequestDTO request
    ) {
        return trackService.batchLookupTracks(request);
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityBindToExistingRequestDTO request
    ) {
        return trackService.bindToExisting(dataSource, externalId, request);
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityCreateAndBindRequestDTO request
    ) {
        return trackService.createAndBind(dataSource, externalId, request);
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindTrack(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        return trackService.unbindTrack(dataSource, externalId);
    }
    
    @DeleteMapping("/unbind/{dataSource}/batch")
    public BatchUnbindResponseDTO batchUnbindTracks(
        @PathVariable DataSource dataSource,
        @Valid @RequestBody BatchUnbindRequestDTO request
    ) {
        return bindingService.batchUnbind(MasterEntityType.TRACK, dataSource, request);
    }
}
