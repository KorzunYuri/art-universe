package yurykorzun.art.universe.music.data.approved.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.ArtistService;

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
    
    @PostMapping("/bind/{dataSource}/{externalId}")
    public ResponseEntity<ResponseWrapper<BoundEntityProjection>> bindArtist(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistBindingRequestDTO request
    ) {
        try {
            BoundEntityProjection result = artistService.bindArtist(dataSource, externalId, request);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to bind artist: %s", e.getMessage()));
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
