package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithTracksSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackReorderRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ExternalAlbumWithTracksCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final BindingService bindingService;

    public AlbumController(AlbumService albumService, BindingService bindingService) {
        this.albumService = albumService;
        this.bindingService = bindingService;
    }

    @GetMapping
    public Page<AlbumDto> findAlbums(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoryId,
        Pageable pageable
    ) {
        return albumService.findAlbums(search, categoryId, pageable);
    }

    @GetMapping("/with-categories")
    public Page<AlbumWithCategoriesDto> findAlbumsWithCategories(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoryId,
        Pageable pageable
    ) {
        return albumService.findAlbumsWithCategories(search, categoryId, pageable);
    }

    @GetMapping("/{id}")
    public AlbumDto getAlbum(@PathVariable Long id) {
        return albumService.getAlbum(id);
    }

    @GetMapping("/{id}/with-categories")
    public AlbumWithCategoriesDto getAlbumWithCategories(@PathVariable Long id) {
        return albumService.getAlbumWithCategories(id);
    }

    @PostMapping
    public AlbumDto saveAlbum(@Valid @RequestBody AlbumSaveRequestDTO request) {
        return albumService.saveAlbum(request);
    }

    @PostMapping("/with-tracks")
    public AlbumDto saveAlbumWithTracks(@Valid @RequestBody AlbumWithTracksSaveRequestDTO request) {
        return albumService.saveAlbumWithTracks(request);
    }

    @DeleteMapping("/{id}")
    public boolean deleteAlbum(@PathVariable Long id) {
        boolean deleted = albumService.deleteAlbum(id);
        if (!deleted) {
            throw new CustomEntityNotFoundException("Album", id);
        }
        return true;
    }

    @PostMapping("/{albumId}/categories/{categoryId}")
    public void bindToCategory(
        @PathVariable Long albumId,
        @PathVariable Long categoryId
    ) {
        albumService.bindToCategory(albumId, categoryId);
    }

    @DeleteMapping("/{albumId}/categories/{categoryId}")
    public void unbindFromCategory(
        @PathVariable Long albumId,
        @PathVariable Long categoryId
    ) {
        albumService.unbindFromCategory(albumId, categoryId);
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

    @PostMapping("/bind/new/{dataSource}/{externalId}/with-tracks")
    public BoundEntityProjection createAndBindWithTracks(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ExternalAlbumWithTracksCreateAndBindRequestDTO request
    ) {
        return albumService.createAndBindWithTracks(dataSource, externalId, request);
    }

    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindAlbum(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        return albumService.unbindAlbum(dataSource, externalId);
    }
    
    @PostMapping("/{albumId}/tracks/copy-from/{sourceAlbumId}")
    public Map<String, Integer> copyTracklist(
        @PathVariable Long albumId,
        @PathVariable Long sourceAlbumId
    ) {
        int copied = albumService.copyTracklist(albumId, sourceAlbumId);
        return Map.of("copiedCount", copied);
    }

    @PutMapping("/{albumId}/tracks/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderTracks(
        @PathVariable Long albumId,
        @Valid @RequestBody TrackReorderRequestDTO request
    ) {
        albumService.reorderTracks(albumId, request.getItems());
    }

    @DeleteMapping("/unbind/{dataSource}/batch")
    public BatchUnbindResponseDTO batchUnbindAlbums(
        @PathVariable DataSource dataSource,
        @Valid @RequestBody BatchUnbindRequestDTO request
    ) {
        return bindingService.batchUnbind(MasterEntityType.ALBUM, dataSource, request);
    }
}
