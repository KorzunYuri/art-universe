package yurykorzun.art.universe.music.data.master.controller;

import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.service.AlbumService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/bound/{dataSource}")
    public List<BoundEntityProjection> findBoundAlbums(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        try {
            return albumService.findBoundAlbums(dataSource, externalIds);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to get bound albums: %s", e.getMessage()), e);
        }
    }
}
