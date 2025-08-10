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
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.service.ArtistService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistService artistService;
    private final BindingService bindingService;

    public ArtistController(ArtistService artistService, BindingService bindingService) {
        this.artistService = artistService;
        this.bindingService = bindingService;
    }

    @GetMapping("/bound/{dataSource}")
    public List<BoundEntityProjection> findBoundArtists(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        return artistService.findBoundArtists(dataSource, externalIds);
    }
    
    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupArtists(
        @RequestParam String search,
        @RequestParam(required = false) Integer limit
    ) {
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();
        return artistService.lookupArtists(request);
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupArtists(
        @Valid @RequestBody BaseBatchLookupRequestDTO request
    ) {
        return artistService.batchLookupArtists(request);
    }
    
    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityBindToExistingRequestDTO request
    ) {
        return artistService.bindToExisting(dataSource, externalId, request);
    }
    
    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityCreateAndBindRequestDTO request
    ) {
        return artistService.createAndBind(dataSource, externalId, request);
    }
    
    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindArtist(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        return artistService.unbindArtist(dataSource, externalId);
    }
    
    @DeleteMapping("/unbind/{dataSource}/batch")
    public BatchUnbindResponseDTO batchUnbindArtists(
        @PathVariable DataSource dataSource,
        @Valid @RequestBody BatchUnbindRequestDTO request
    ) {
        return bindingService.batchUnbind(EntityType.ARTIST, dataSource, request);
    }
}
