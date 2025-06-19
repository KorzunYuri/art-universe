package yurykorzun.art.universe.music.data.approved.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
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
}
