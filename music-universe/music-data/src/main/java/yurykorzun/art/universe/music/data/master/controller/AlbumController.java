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
        return albumService.findBoundAlbums(dataSource, externalIds);
    }
    
    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupAlbums(
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
        return albumService.lookupAlbums(request);
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupAlbums(
        @Valid @RequestBody ArtistRelatedBatchLookupRequestDTO request
    ) {
        return albumService.batchLookupAlbums(request);
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityBindToExistingRequestDTO request
    ) {
        return albumService.bindToExisting(dataSource, externalId, request);
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityCreateAndBindRequestDTO request
    ) {
        return albumService.createAndBind(dataSource, externalId, request);
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindAlbum(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        return albumService.unbindAlbum(dataSource, externalId);
    }
}
