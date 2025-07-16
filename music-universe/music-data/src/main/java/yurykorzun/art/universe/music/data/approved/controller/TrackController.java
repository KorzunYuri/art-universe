package yurykorzun.art.universe.music.data.approved.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TrackBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.TrackCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.TrackService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/bound/{dataSource}")
    public ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> findBoundTracks(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        try {
            List<BoundEntityProjection> result = trackService.findBoundTracks(dataSource, externalIds);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to get bound tracks: %s", e.getMessage()));
        }
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<BoundEntityProjection>> bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody TrackBindToExistingRequestDTO request
    ) {
        try {
            BoundEntityProjection result = trackService.bindToExisting(dataSource, externalId, request);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to bind track to existing: %s", e.getMessage()));
        }
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<BoundEntityProjection>> createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody TrackCreateAndBindRequestDTO request
    ) {
        try {
            BoundEntityProjection result = trackService.createAndBind(dataSource, externalId, request);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to create and bind track: %s", e.getMessage()));
        }
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<Boolean>> unbindTrack(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        try {
            boolean result = trackService.unbindTrack(dataSource, externalId);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to unbind track: %s", e.getMessage()));
        }
    }
}
