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
    
    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupAlbums(
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
            return albumService.lookupAlbums(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to lookup albums: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupAlbums(
        @Valid @RequestBody ArtistRelatedBatchLookupRequestDTO request
    ) {
        try {
            return albumService.batchLookupAlbums(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to batch lookup albums: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityBindToExistingRequestDTO request
    ) {
        try {
            return albumService.bindToExisting(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to bind album to existing: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityCreateAndBindRequestDTO request
    ) {
        try {
            return albumService.createAndBind(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to create and bind album: %s", e.getMessage()), e);
        }
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindAlbum(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        try {
            return albumService.unbindAlbum(dataSource, externalId);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to unbind album: %s", e.getMessage()), e);
        }
    }
}
