package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.*;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.ArtistService;
import yurykorzun.art.universe.music.data.master.service.CategoryService;
import yurykorzun.art.universe.music.data.master.service.TrackService;

/**
 * Internal API for ETL pipelines.
 * All entities created through this controller have origin=ETL.
 * The approval status is configurable per request (defaults to NEW for entities, PENDING for bindings).
 */
@RestController
@RequestMapping("/api/v1/internal")
public class InternalEntityController {

    private final ArtistService artistService;
    private final AlbumService albumService;
    private final TrackService trackService;
    private final CategoryService categoryService;

    public InternalEntityController(
        ArtistService artistService,
        AlbumService albumService,
        TrackService trackService,
        CategoryService categoryService
    ) {
        this.artistService = artistService;
        this.albumService = albumService;
        this.trackService = trackService;
        this.categoryService = categoryService;
    }

    // --- Artists ---

    @PostMapping("/artists")
    public ArtistDto createArtist(
        @Valid @RequestBody ArtistSaveRequestDTO request,
        @RequestParam(defaultValue = "new") MasterApprovalStatus approvalStatus
    ) {
        return artistService.saveArtist(request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/artists/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindArtistToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityBindToExistingRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return artistService.bindToExisting(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/artists/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBindArtist(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityCreateAndBindRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return artistService.createAndBind(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    // --- Albums ---

    @PostMapping("/albums")
    public AlbumDto createAlbum(
        @Valid @RequestBody AlbumSaveRequestDTO request,
        @RequestParam(defaultValue = "new") MasterApprovalStatus approvalStatus
    ) {
        return albumService.saveAlbum(request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/albums/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindAlbumToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityBindToExistingRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return albumService.bindToExisting(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/albums/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBindAlbum(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityCreateAndBindRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return albumService.createAndBind(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/albums/bind/new/{dataSource}/{externalId}/with-tracks")
    public BoundEntityProjection createAndBindAlbumWithTracks(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ExternalAlbumWithTracksCreateAndBindRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return albumService.createAndBindWithTracks(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    // --- Tracks ---

    @PostMapping("/tracks")
    public TrackDto createTrack(
        @Valid @RequestBody TrackSaveRequestDTO request,
        @RequestParam(defaultValue = "new") MasterApprovalStatus approvalStatus
    ) {
        return trackService.saveTrack(request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/tracks/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindTrackToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityBindToExistingRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return trackService.bindToExisting(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/tracks/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBindTrack(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody ArtistRelatedEntityCreateAndBindRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return trackService.createAndBind(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    // --- Categories ---

    @PostMapping("/categories")
    public CategoryDto createCategory(
        @Valid @RequestBody CategorySaveRequestDTO request,
        @RequestParam(defaultValue = "new") MasterApprovalStatus approvalStatus
    ) {
        return categoryService.saveCategory(request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/categories/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindCategoryToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityBindToExistingRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return categoryService.bindToExisting(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }

    @PostMapping("/categories/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBindCategory(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityCreateAndBindRequestDTO request,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return categoryService.createAndBind(dataSource, externalId, request, Origin.ETL, approvalStatus);
    }
}
