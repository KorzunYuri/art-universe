package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
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
    public List<BoundEntityProjection> findBoundArtists(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        try {
            return artistService.findBoundArtists(dataSource, externalIds);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to get bound artists: %s", e.getMessage()), e);
        }
    }
    
    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupArtists(
        @RequestParam String search,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            LookupRequestDTO request = LookupRequestDTO.builder()
                .search(search)
                .limit(limit)
                .build();
            return artistService.lookupArtists(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to lookup artists: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupArtists(
        @Valid @RequestBody BaseBatchLookupRequestDTO request
    ) {
        try {
            return artistService.batchLookupArtists(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to batch lookup artists: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityBindToExistingRequestDTO request
    ) {
        try {
            return artistService.bindToExisting(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to bind artist to existing: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityCreateAndBindRequestDTO request
    ) {
        try {
            return artistService.createAndBind(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to create and bind artist: %s", e.getMessage()), e);
        }
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindArtist(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        try {
            return artistService.unbindArtist(dataSource, externalId);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to unbind artist: %s", e.getMessage()), e);
        }
    }
}
