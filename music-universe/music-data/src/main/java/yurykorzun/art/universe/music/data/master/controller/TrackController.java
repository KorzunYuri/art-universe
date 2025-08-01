package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.service.TrackService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/bound/{dataSource}")
    public List<BoundEntityProjection> findBoundTracks(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        try {
            return trackService.findBoundTracks(dataSource, externalIds);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to get bound tracks: %s", e.getMessage()), e);
        }
    }
    
    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupTracks(
        @RequestParam String search,
        @RequestParam(required = false) Long masterArtistId,
        @RequestParam(required = false) Long externalArtistId,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search(search)
                .masterArtistId(masterArtistId)
                .externalArtistId(externalArtistId)
                .limit(limit)
                .build();
            return trackService.lookupTracks(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to lookup tracks: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupTracks(
        @Valid @RequestBody ArtistRelatedBatchLookupRequestDTO request
    ) {
        try {
            return trackService.batchLookupTracks(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to batch lookup tracks: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityBindToExistingRequestDTO request
    ) {
        try {
            return trackService.bindToExisting(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to bind track to existing: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityCreateAndBindRequestDTO request
    ) {
        try {
            return trackService.createAndBind(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to create and bind track: %s", e.getMessage()), e);
        }
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindTrack(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        try {
            return trackService.unbindTrack(dataSource, externalId);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to unbind track: %s", e.getMessage()), e);
        }
    }
}
