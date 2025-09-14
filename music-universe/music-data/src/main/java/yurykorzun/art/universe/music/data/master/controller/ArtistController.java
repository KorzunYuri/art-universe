package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistDto;
import yurykorzun.art.universe.music.data.master.dto.ArtistSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
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

    @GetMapping
    public Page<ArtistDto> findArtists(
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        return artistService.findArtists(search, pageable);
    }

    @GetMapping("/with-categories")
    public Page<ArtistWithCategoriesDto> findArtistsWithCategories(
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        return artistService.findArtistsWithCategories(search, pageable);
    }

    @GetMapping("/{id}")
    public ArtistDto getArtist(
        @PathVariable Long id
    ) {
        return artistService.getArtist(id);
    }

    @PostMapping
    public ArtistDto saveArtist(
        @Valid @RequestBody ArtistSaveRequestDTO request
    ) {
        return artistService.saveArtist(request);
    }

    @DeleteMapping("/{id}")
    public boolean deleteArtist(@PathVariable Long id) {
        boolean deleted = artistService.deleteArtist(id);
        if (!deleted) {
            throw new CustomEntityNotFoundException("Artist", id);
        }
        return true;
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
        return bindingService.batchUnbind(MasterEntityType.ARTIST, dataSource, request);
    }
}
