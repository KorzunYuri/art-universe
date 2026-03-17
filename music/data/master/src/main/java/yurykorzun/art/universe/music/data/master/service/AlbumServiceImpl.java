package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.*;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;
import yurykorzun.art.universe.music.data.master.repository.*;
import yurykorzun.art.universe.music.data.master.service.lookup.ArtistRelatedLookupService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumBindingRepository bindingsRepository;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final AlbumTrackRepository albumTrackRepository;
    private final RelationTypeApplicabilityRepository relationTypeApplicabilityRepository;
    private final TrackRepository trackRepository;
    private final TrackBindingRepository trackBindingRepository;
    private final ArtistService artistService;
    private final RelationService relationService;
    private final ArtistRelatedLookupService lookupService;

    public AlbumServiceImpl(
        AlbumRepository albumRepository,
        AlbumBindingRepository bindingsRepository,
        AlbumCategoryRepository albumCategoryRepository,
        CategoryRepository categoryRepository,
        AlbumTrackRepository albumTrackRepository,
        RelationTypeApplicabilityRepository relationTypeApplicabilityRepository,
        TrackRepository trackRepository,
        TrackBindingRepository trackBindingRepository,
        ArtistService artistService,
        RelationService relationService,
        EntityManager entityManager
    ) {
        this.albumRepository = albumRepository;
        this.bindingsRepository = bindingsRepository;
        this.albumCategoryRepository = albumCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.albumTrackRepository = albumTrackRepository;
        this.relationTypeApplicabilityRepository = relationTypeApplicabilityRepository;
        this.trackRepository = trackRepository;
        this.trackBindingRepository = trackBindingRepository;
        this.artistService = artistService;
        this.relationService = relationService;
        this.lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.ALBUM);
    }

    @Override
    public Page<AlbumDto> findAlbums(String search, Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            return albumRepository.findAlbums(search, pageable)
                    .map(this::mapToDto);
        } else {
            return albumRepository.findAlbums(search, categoryId, pageable)
                    .map(this::mapToDto);
        }
    }

    @Override
    public Page<AlbumWithCategoriesDto> findAlbumsWithCategories(String search, Long categoryId, Pageable pageable) {
        return albumRepository.findAlbumsWithCategories(search, categoryId, pageable)
                .map(this::mapToAlbumWithCategories);
    }

    @Override
    public AlbumDto getAlbum(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Album", id));
        return mapToDto(album);
    }

    @Override
    public AlbumWithCategoriesDto getAlbumWithCategories(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Album", id));
        return mapToAlbumWithCategories(album);
    }

    @Override
    @Transactional
    public AlbumDto saveAlbum(AlbumSaveRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        Album album;
        if (request.getId() != null) {
            // Update existing album
            album = albumRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomEntityNotFoundException("Album", request.getId()));
            album.setName(request.getName());
            if (request.getPrimaryArtistId() != null) {
                album.setPrimaryArtistId(request.getPrimaryArtistId());
            }
        } else {
            // Create new album — primaryArtistId is required
            if (request.getPrimaryArtistId() == null) {
                throw new IllegalArgumentException("primaryArtistId is required when creating a new album");
            }
            album = Album.builder()
                    .name(request.getName())
                    .primaryArtistId(request.getPrimaryArtistId())
                    .origin(origin)
                    .approvalStatus(approvalStatus)
                    .build();
        }

        Album savedAlbum = albumRepository.save(album);
        return mapToDto(savedAlbum);
    }

    @Override
    @Transactional
    public AlbumDto saveAlbumWithTracks(AlbumWithTracksSaveRequestDTO request) {
        // Save or update the album itself
        AlbumSaveRequestDTO albumRequest = AlbumSaveRequestDTO.builder()
                .id(request.getId())
                .name(request.getName())
                .primaryArtistId(request.getPrimaryArtistId())
                .build();
        AlbumDto savedAlbum = saveAlbum(albumRequest, Origin.MANUAL, MasterApprovalStatus.APPROVED);
        Long albumId = savedAlbum.getId();

        // Resolve default relation type for ALBUM → TRACK
        Long defaultRelationTypeId = relationTypeApplicabilityRepository
                .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(
                        MasterEntityType.ALBUM, MasterEntityType.TRACK)
                .map(RelationTypeApplicability::getRelationTypeId)
                .orElse(null);

        // Create or update album-track relations
        for (AlbumTrackItemDTO trackItem : request.getTracks()) {
            Long trackId = trackItem.getTrackId();

            // Validate track exists
            if (!trackRepository.existsById(trackId)) {
                throw new CustomEntityNotFoundException("Track", trackId);
            }

            // Determine relation type: use provided or fall back to default
            Long relationTypeId = trackItem.getRelationTypeId() != null
                    ? trackItem.getRelationTypeId()
                    : defaultRelationTypeId;

            // Find existing album-track relation or create new one
            var existing = albumTrackRepository
                    .findByAlbumIdAndTrackIdAndRelationTypeId(albumId, trackId, relationTypeId);

            if (existing.isPresent()) {
                // Update track order
                AlbumTrack albumTrack = existing.get();
                albumTrack.setTrackOrder(trackItem.getTrackOrder());
                albumTrackRepository.save(albumTrack);
            } else {
                // Create new album-track relation
                AlbumTrack albumTrack = AlbumTrack.builder()
                        .albumId(albumId)
                        .trackId(trackId)
                        .trackOrder(trackItem.getTrackOrder())
                        .relationTypeId(relationTypeId)
                        .build();
                albumTrackRepository.save(albumTrack);
            }
        }

        return savedAlbum;
    }

    @Override
    @Transactional
    public boolean deleteAlbum(Long id) {
        if (albumRepository.existsById(id)) {
            albumRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void bindToCategory(Long albumId, Long categoryId, Origin origin, MasterApprovalStatus approvalStatus) {
        if (!albumRepository.existsById(albumId)) {
            throw new CustomEntityNotFoundException("Album", albumId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }
        if (albumCategoryRepository.existsByAlbumIdAndCategoryId(albumId, categoryId)) {
            throw new IllegalArgumentException("Relation between album " + albumId + " and category " + categoryId + " already exists");
        }

        AlbumCategory relation = AlbumCategory.builder()
            .albumId(albumId)
            .categoryId(categoryId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        albumCategoryRepository.save(relation);
    }

    @Override
    @Transactional
    public void unbindFromCategory(Long albumId, Long categoryId) {
        if (!albumRepository.existsById(albumId)) {
            throw new CustomEntityNotFoundException("Album", albumId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }

        AlbumCategory relation = albumCategoryRepository.findByAlbumIdAndCategoryId(albumId, categoryId)
            .orElseThrow(() -> new CustomEntityNotFoundException("Relation between album " + albumId + " and category " + categoryId + " not found"));
        albumCategoryRepository.delete(relation);
    }

    private AlbumDto mapToDto(Album album) {
        return AlbumDto.builder()
                .id(album.getId())
                .name(album.getName())
                .primaryArtistId(album.getPrimaryArtistId())
                .build();
    }

    private AlbumWithCategoriesDto mapToAlbumWithCategories(Album album) {
        List<CategoryDto> categories = List.of();
        if (album.getCategoryRelations() != null) {
            categories = album.getCategoryRelations().stream()
                    .map(relation -> CategoryDto.builder()
                            .id(relation.getCategory().getId())
                            .name(relation.getCategory().getName())
                            .build())
                    .toList();
        }

        return AlbumWithCategoriesDto.builder()
                .id(album.getId())
                .name(album.getName())
                .primaryArtistId(album.getPrimaryArtistId())
                .categories(categories)
                .build();
    }

    @Override
    public List<BoundEntityProjection> findBoundAlbums(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, externalIds);
    }

    @Override
    @Transactional
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        Album album = albumRepository.findById(request.getMasterId())
            .orElseThrow(() -> new CustomEntityNotFoundException("Album", request.getMasterId()));
        Long masterArtistId = album.getPrimaryArtistId();

        Optional<AlbumBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);

        if (existingBinding.isPresent()) {
            AlbumBinding binding = existingBinding.get();
            if (!binding.getMasterId().equals(album.getId())) {
                binding.setMasterId(album.getId());
                bindingsRepository.save(binding);
            }
        } else {
            AlbumBinding binding = AlbumBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .masterId(album.getId())
                .origin(origin)
                .approvalStatus(approvalStatus)
                .build();
            bindingsRepository.save(binding);
        }

        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId, MasterEntityType.ALBUM, album.getId(), null, origin, approvalStatus);

        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, List.of(externalId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Album binding not found after creation"));
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        Long masterArtistId = resolveMasterArtistId(request.getMasterPrimaryArtistId());

        // Guard against duplicate album name under the same artist
        Optional<Album> existingAlbum = albumRepository.findByNameAndPrimaryArtistId(
            request.getEntityName(), masterArtistId);
        if (existingAlbum.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "Album with name '%s' for artist ID %d already exists",
                request.getEntityName(), masterArtistId));
        }

        // Guard against duplicate external binding
        Optional<AlbumBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        if (existingBinding.isPresent()) {
            throw new IllegalStateException(String.format(
                "Album binding for external ID %d from %s already exists",
                externalId, dataSource));
        }

        // Create master album
        Album album = Album.builder()
            .name(request.getEntityName())
            .primaryArtistId(masterArtistId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        Album savedAlbum = albumRepository.save(album);

        // Create external binding
        AlbumBinding binding = AlbumBinding.builder()
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(savedAlbum.getId())
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        bindingsRepository.save(binding);

        // Create ARTIST → ALBUM internal relation
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId,
            MasterEntityType.ALBUM, savedAlbum.getId(),
            null, origin, approvalStatus);

        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, List.of(externalId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Album binding not found after creation"));
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBindWithTracks(DataSource dataSource, Long externalAlbumId, ExternalAlbumWithTracksCreateAndBindRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        // 1. Resolve master artist
        Long masterArtistId = resolveMasterArtistId(request.getMasterPrimaryArtistId());

        // 2. Resolve master album (existing or new)
        Long albumId = resolveMasterAlbum(dataSource, externalAlbumId, request, masterArtistId, origin, approvalStatus);

        // 3. Resolve default ALBUM → TRACK relation type once for all tracks
        Long defaultRelationTypeId = relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(
                MasterEntityType.ALBUM, MasterEntityType.TRACK)
            .map(RelationTypeApplicability::getRelationTypeId)
            .orElse(null);

        // 4. Process each track in the request
        for (ExternalAlbumTrackItemDTO trackItem : request.getTracks()) {
            Long masterTrackId = resolveMasterTrack(dataSource, trackItem, masterArtistId, origin, approvalStatus);

            // Upsert ALBUM → TRACK relation
            Long relationTypeId = trackItem.getRelationTypeId() != null
                ? trackItem.getRelationTypeId()
                : defaultRelationTypeId;

            Optional<AlbumTrack> existingAlbumTrack = albumTrackRepository
                .findByAlbumIdAndTrackIdAndRelationTypeId(albumId, masterTrackId, relationTypeId);

            if (existingAlbumTrack.isPresent()) {
                AlbumTrack albumTrack = existingAlbumTrack.get();
                albumTrack.setTrackOrder(trackItem.getTrackOrder());
                albumTrackRepository.save(albumTrack);
            } else {
                AlbumTrack albumTrack = AlbumTrack.builder()
                    .albumId(albumId)
                    .trackId(masterTrackId)
                    .trackOrder(trackItem.getTrackOrder())
                    .relationTypeId(relationTypeId)
                    .build();
                albumTrackRepository.save(albumTrack);
            }
        }

        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, List.of(externalAlbumId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Album binding not found after creation"));
    }

    /**
     * Resolves the master album ID — either from an existing binding/master ID or by creating a new one.
     */
    private Long resolveMasterAlbum(
        DataSource dataSource,
        Long externalAlbumId,
        ExternalAlbumWithTracksCreateAndBindRequestDTO request,
        Long masterArtistId,
        Origin origin,
        MasterApprovalStatus approvalStatus
    ) {
        // Case 1: Master album ID provided directly — album is already bound
        if (request.getMasterAlbumId() != null) {
            if (!albumRepository.existsById(request.getMasterAlbumId())) {
                throw new CustomEntityNotFoundException("Album", request.getMasterAlbumId());
            }
            log.info("Using existing master album ID {} for external album {}", request.getMasterAlbumId(), externalAlbumId);
            return request.getMasterAlbumId();
        }

        // Case 2: Check if external album is already bound
        Optional<AlbumBinding> existingAlbumBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalAlbumId);
        if (existingAlbumBinding.isPresent()) {
            Long existingMasterId = existingAlbumBinding.get().getMasterId();
            log.warn("External album {} from {} is already bound to master album {}. Using existing binding.",
                externalAlbumId, dataSource, existingMasterId);
            return existingMasterId;
        }

        // Case 3: Create new master album
        if (request.getAlbumName() == null || request.getAlbumName().isBlank()) {
            throw new IllegalArgumentException("albumName is required when masterAlbumId is not provided");
        }

        Album album = Album.builder()
            .name(request.getAlbumName())
            .primaryArtistId(masterArtistId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        Album savedAlbum = albumRepository.save(album);
        Long albumId = savedAlbum.getId();

        // Bind external album
        AlbumBinding albumBinding = AlbumBinding.builder()
            .dataSource(dataSource)
            .externalId(externalAlbumId)
            .masterId(albumId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        bindingsRepository.save(albumBinding);

        // Create ARTIST → ALBUM internal relation
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId,
            MasterEntityType.ALBUM, albumId,
            null, origin, approvalStatus);

        return albumId;
    }

    /**
     * Resolves the master track ID for a single track item.
     * <ul>
     *   <li>Bound: verifies the master track exists; creates the external track binding if absent.</li>
     *   <li>Unbound: creates a new master track, binds the external track, and wires internal relations.</li>
     * </ul>
     */
    private Long resolveMasterTrack(
        DataSource dataSource,
        ExternalAlbumTrackItemDTO trackItem,
        Long albumMasterArtistId,
        Origin origin,
        MasterApprovalStatus approvalStatus
    ) {
        // Check if external track is already bound
        Optional<TrackBinding> existingTrackBinding =
            trackBindingRepository.findByDataSourceAndExternalId(dataSource, trackItem.getExternalTrackId());

        if (trackItem.getMasterTrackId() != null) {
            // --- Bound track (master ID provided explicitly) ---
            Long masterTrackId = trackItem.getMasterTrackId();
            if (!trackRepository.existsById(masterTrackId)) {
                throw new CustomEntityNotFoundException("Track", masterTrackId);
            }
            // Create external binding only if it doesn't already exist
            if (existingTrackBinding.isEmpty()) {
                TrackBinding trackBinding = TrackBinding.builder()
                    .dataSource(dataSource)
                    .externalId(trackItem.getExternalTrackId())
                    .masterId(masterTrackId)
                    .origin(origin)
                    .approvalStatus(approvalStatus)
                    .build();
                trackBindingRepository.save(trackBinding);
            }
            return masterTrackId;
        }

        // --- Check if external track is already bound (no masterTrackId provided) ---
        if (existingTrackBinding.isPresent()) {
            Long existingMasterId = existingTrackBinding.get().getMasterId();
            log.warn("External track {} from {} is already bound to master track {}. Using existing binding.",
                trackItem.getExternalTrackId(), dataSource, existingMasterId);
            return existingMasterId;
        }

        // --- Unbound track: create new master track ---
        if (trackItem.getTrackName() == null || trackItem.getTrackName().isBlank()) {
            throw new IllegalArgumentException(
                "trackName is required for unbound tracks (externalTrackId=" + trackItem.getExternalTrackId() + ")");
        }

        // Prefer per-track artist override; fall back to album's master artist
        Long effectiveArtistId = trackItem.getMasterPrimaryArtistId() != null
            ? trackItem.getMasterPrimaryArtistId()
            : albumMasterArtistId;

        // Create master track
        Track track = Track.builder()
            .name(trackItem.getTrackName())
            .primaryArtistId(effectiveArtistId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        Track savedTrack = trackRepository.save(track);
        Long masterTrackId = savedTrack.getId();

        // Bind external track
        TrackBinding trackBinding = TrackBinding.builder()
            .dataSource(dataSource)
            .externalId(trackItem.getExternalTrackId())
            .masterId(masterTrackId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        trackBindingRepository.save(trackBinding);

        // Create ARTIST → TRACK internal relation
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, effectiveArtistId,
            MasterEntityType.TRACK, masterTrackId,
            null, origin, approvalStatus);

        return masterTrackId;
    }

    @Override
    @Transactional
    public int copyTracklist(Long targetAlbumId, Long sourceAlbumId) {
        if (!albumRepository.existsById(targetAlbumId)) {
            throw new CustomEntityNotFoundException("Album", targetAlbumId);
        }
        if (!albumRepository.existsById(sourceAlbumId)) {
            throw new CustomEntityNotFoundException("Album", sourceAlbumId);
        }

        List<AlbumTrack> sourceTracks = albumTrackRepository.findByAlbumId(sourceAlbumId);
        int copiedCount = 0;

        for (AlbumTrack source : sourceTracks) {
            Optional<AlbumTrack> existing = albumTrackRepository
                .findByAlbumIdAndTrackIdAndRelationTypeId(targetAlbumId, source.getTrackId(), source.getRelationTypeId());

            if (existing.isEmpty()) {
                AlbumTrack copy = AlbumTrack.builder()
                    .albumId(targetAlbumId)
                    .trackId(source.getTrackId())
                    .trackOrder(source.getTrackOrder())
                    .relationTypeId(source.getRelationTypeId())
                    .build();
                albumTrackRepository.save(copy);
                copiedCount++;
            } else {
                log.debug("Skipping track {} — already exists in album {}", source.getTrackId(), targetAlbumId);
            }
        }

        return copiedCount;
    }

    @Override
    @Transactional
    public void reorderTracks(Long albumId, List<TrackReorderItemDTO> items) {
        if (!albumRepository.existsById(albumId)) {
            throw new CustomEntityNotFoundException("Album", albumId);
        }

        // Validate no duplicate newOrder values in the request
        long distinctOrders = items.stream().map(TrackReorderItemDTO::getNewOrder).distinct().count();
        if (distinctOrders < items.size()) {
            throw new IllegalArgumentException("Duplicate newOrder values are not allowed in a reorder request");
        }

        for (TrackReorderItemDTO item : items) {
            AlbumTrack albumTrack = albumTrackRepository.findById(item.getAlbumTrackId())
                .orElseThrow(() -> new CustomEntityNotFoundException("AlbumTrack", item.getAlbumTrackId()));

            if (!albumTrack.getAlbumId().equals(albumId)) {
                throw new IllegalArgumentException(String.format(
                    "AlbumTrack %d does not belong to album %d", item.getAlbumTrackId(), albumId));
            }

            albumTrack.setTrackOrder(item.getNewOrder());
            albumTrackRepository.save(albumTrack);
        }
    }

    @Override
    @Transactional
    public boolean unbindAlbum(DataSource dataSource, Long externalId) {
        Optional<AlbumBinding> binding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        if (binding.isPresent()) {
            bindingsRepository.delete(binding.get());
            return true;
        }
        return false;
    }

    @Override
    public List<LookupResultDTO> lookupAlbums(ArtistRelatedLookupRequestDTO request) {
        return lookupService.lookup(request);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchLookupResponseDTO batchLookupAlbums(ArtistRelatedBatchLookupRequestDTO request) {
        return lookupService.batchLookup(request);
    }

    /**
     * Validates and returns the master artist ID.
     *
     * @param masterPrimaryArtistId master artist ID — required
     * @return the master artist ID
     * @throws IllegalArgumentException if not provided or artist does not exist
     */
    private Long resolveMasterArtistId(Long masterPrimaryArtistId) {
        if (masterPrimaryArtistId == null) {
            throw new IllegalArgumentException("masterPrimaryArtistId is required");
        }
        artistService.getArtist(masterPrimaryArtistId);
        return masterPrimaryArtistId;
    }
}
