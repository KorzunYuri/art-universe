package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.master.dto.ArtistBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.service.ArtistService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping("/bound/{dataSource}")
    public ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> findBoundArtists(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        try {
            List<BoundEntityProjection> result = artistService.findBoundArtists(dataSource, externalIds);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to get bound artists: %s", e.getMessage()));
        }
    }
    
    @GetMapping("/lookup")
    public ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> lookupArtists(
        @RequestParam String name,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            List<LookupResultDTO> result = limit != null
                ? artistService.searchArtistsByName(name, limit)
                : artistService.searchArtistsByName(name);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to lookup artists: %s", e.getMessage()));
        }
    }
    
    @PostMapping("/lookup/batch")
    public ResponseEntity<ResponseWrapper<ArtistBatchLookupResponseDTO>> batchLookupArtists(
        @Valid @RequestBody ArtistBatchLookupRequestDTO request
    ) {
        try {
            ArtistBatchLookupResponseDTO result = artistService.batchLookupArtists(request);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to batch lookup artists: %s", e.getMessage()));
        }
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<BoundEntityProjection>> bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistBindToExistingRequestDTO request
    ) {
        try {
            BoundEntityProjection result = artistService.bindToExisting(dataSource, externalId, request);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to bind artist to existing: %s", e.getMessage()));
        }
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<BoundEntityProjection>> createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistCreateAndBindRequestDTO request
    ) {
        try {
            BoundEntityProjection result = artistService.createAndBind(dataSource, externalId, request);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to create and bind artist: %s", e.getMessage()));
        }
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<Boolean>> unbindArtist(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        try {
            boolean result = artistService.unbindArtist(dataSource, externalId);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to unbind artist: %s", e.getMessage()));
        }
    }
}
