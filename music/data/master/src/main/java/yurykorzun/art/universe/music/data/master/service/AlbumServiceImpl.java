package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumTrackItemDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithTracksSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.ExternalAlbumTrackItemDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ExternalAlbumWithTracksCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.Album;
import yurykorzun.art.universe.music.data.master.entity.AlbumBinding;
import yurykorzun.art.universe.music.data.master.entity.AlbumCategory;
import yurykorzun.art.universe.music.data.master.entity.AlbumTrack;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.music.data.master.entity.TrackBinding;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumTrackRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeApplicabilityRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.ArtistRelatedLookupService;

import java.util.List;
import java.util.Optional;

@Service
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
    public AlbumDto saveAlbum(AlbumSaveRequestDTO request) {
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
        AlbumDto savedAlbum = saveAlbum(albumRequest);
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
    public void bindToCategory(Long albumId, Long categoryId) {
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
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request) {
        throw new UnsupportedOperationException("Album bindToExisting is not yet implemented");
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request) {
        // Look up master artist by external ID
        BoundEntityProjection artistBinding = artistService.findArtist(dataSource, request.getPrimaryArtistId());
        if (artistBinding == null) {
            throw new IllegalStateException(String.format(
                "Artist with external ID %d from %s must be bound before binding album",
                request.getPrimaryArtistId(), dataSource));
        }
        Long masterArtistId = artistBinding.getMasterId();

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
            .build();
        Album savedAlbum = albumRepository.save(album);

        // Create external binding
        AlbumBinding binding = AlbumBinding.builder()
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(savedAlbum.getId())
            .build();
        bindingsRepository.save(binding);

        // Create ARTIST → ALBUM internal relation
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId,
            MasterEntityType.ALBUM, savedAlbum.getId(),
            null);

        // Bind external ARTIST → ALBUM relation
        relationService.bindExternalRelation(
            dataSource,
            MasterEntityType.ARTIST, request.getPrimaryArtistId(),
            MasterEntityType.ALBUM, externalId);

        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, List.of(externalId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Album binding not found after creation"));
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBindWithTracks(DataSource dataSource, Long externalAlbumId, ExternalAlbumWithTracksCreateAndBindRequestDTO request) {
        // 1. Look up master artist for the album
        BoundEntityProjection artistBinding = artistService.findArtist(dataSource, request.getPrimaryArtistId());
        if (artistBinding == null) {
            throw new IllegalStateException(String.format(
                "Artist with external ID %d from %s must be bound before binding album",
                request.getPrimaryArtistId(), dataSource));
        }
        Long masterArtistId = artistBinding.getMasterId();

        // 2. Guard against duplicate external album binding
        Optional<AlbumBinding> existingAlbumBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalAlbumId);
        if (existingAlbumBinding.isPresent()) {
            throw new IllegalStateException(String.format(
                "Album binding for external ID %d from %s already exists",
                externalAlbumId, dataSource));
        }

        // 3. Create master album
        Album album = Album.builder()
            .name(request.getAlbumName())
            .primaryArtistId(masterArtistId)
            .build();
        Album savedAlbum = albumRepository.save(album);
        Long albumId = savedAlbum.getId();

        // 4. Bind external album
        AlbumBinding albumBinding = AlbumBinding.builder()
            .dataSource(dataSource)
            .externalId(externalAlbumId)
            .masterId(albumId)
            .build();
        bindingsRepository.save(albumBinding);

        // 5. Create ARTIST → ALBUM internal relation
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId,
            MasterEntityType.ALBUM, albumId,
            null);

        // 6. Bind external ARTIST → ALBUM relation
        relationService.bindExternalRelation(
            dataSource,
            MasterEntityType.ARTIST, request.getPrimaryArtistId(),
            MasterEntityType.ALBUM, externalAlbumId);

        // 7. Resolve default ALBUM → TRACK relation type once for all tracks
        Long defaultRelationTypeId = relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(
                MasterEntityType.ALBUM, MasterEntityType.TRACK)
            .map(RelationTypeApplicability::getRelationTypeId)
            .orElse(null);

        // 8. Process each track in the request
        for (ExternalAlbumTrackItemDTO trackItem : request.getTracks()) {
            Long masterTrackId = resolveMasterTrack(dataSource, trackItem, masterArtistId, request.getPrimaryArtistId());

            // Create ALBUM → TRACK relation
            Long relationTypeId = trackItem.getRelationTypeId() != null
                ? trackItem.getRelationTypeId()
                : defaultRelationTypeId;

            AlbumTrack albumTrack = AlbumTrack.builder()
                .albumId(albumId)
                .trackId(masterTrackId)
                .trackOrder(trackItem.getTrackOrder())
                .relationTypeId(relationTypeId)
                .build();
            albumTrackRepository.save(albumTrack);
        }

        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, List.of(externalAlbumId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Album binding not found after creation"));
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
        Long albumExternalArtistId
    ) {
        if (trackItem.getMasterTrackId() != null) {
            // --- Bound track ---
            Long masterTrackId = trackItem.getMasterTrackId();
            if (!trackRepository.existsById(masterTrackId)) {
                throw new CustomEntityNotFoundException("Track", masterTrackId);
            }
            // Create external binding only if it doesn't already exist
            Optional<TrackBinding> existingTrackBinding =
                trackBindingRepository.findByDataSourceAndExternalId(dataSource, trackItem.getExternalTrackId());
            if (existingTrackBinding.isEmpty()) {
                TrackBinding trackBinding = TrackBinding.builder()
                    .dataSource(dataSource)
                    .externalId(trackItem.getExternalTrackId())
                    .masterId(masterTrackId)
                    .build();
                trackBindingRepository.save(trackBinding);
            }
            return masterTrackId;
        }

        // --- Unbound track ---
        if (trackItem.getTrackName() == null || trackItem.getTrackName().isBlank()) {
            throw new IllegalArgumentException(
                "trackName is required for unbound tracks (externalTrackId=" + trackItem.getExternalTrackId() + ")");
        }

        // Resolve track artist: use per-track override or fall back to album's artist
        Long trackMasterArtistId;
        Long trackExternalArtistId;
        if (trackItem.getPrimaryArtistId() != null) {
            BoundEntityProjection trackArtistBinding = artistService.findArtist(dataSource, trackItem.getPrimaryArtistId());
            if (trackArtistBinding == null) {
                throw new IllegalStateException(String.format(
                    "Artist with external ID %d from %s must be bound before creating track '%s'",
                    trackItem.getPrimaryArtistId(), dataSource, trackItem.getTrackName()));
            }
            trackMasterArtistId = trackArtistBinding.getMasterId();
            trackExternalArtistId = trackItem.getPrimaryArtistId();
        } else {
            trackMasterArtistId = albumMasterArtistId;
            trackExternalArtistId = albumExternalArtistId;
        }

        // Create master track
        Track track = Track.builder()
            .name(trackItem.getTrackName())
            .primaryArtistId(trackMasterArtistId)
            .build();
        Track savedTrack = trackRepository.save(track);
        Long masterTrackId = savedTrack.getId();

        // Bind external track
        TrackBinding trackBinding = TrackBinding.builder()
            .dataSource(dataSource)
            .externalId(trackItem.getExternalTrackId())
            .masterId(masterTrackId)
            .build();
        trackBindingRepository.save(trackBinding);

        // Create ARTIST → TRACK internal relation
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, trackMasterArtistId,
            MasterEntityType.TRACK, masterTrackId,
            null);

        // Bind external ARTIST → TRACK relation
        relationService.bindExternalRelation(
            dataSource,
            MasterEntityType.ARTIST, trackExternalArtistId,
            MasterEntityType.TRACK, trackItem.getExternalTrackId());

        return masterTrackId;
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
}
