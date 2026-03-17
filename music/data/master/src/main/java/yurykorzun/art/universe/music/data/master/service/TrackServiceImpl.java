package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.TrackDto;
import yurykorzun.art.universe.music.data.master.dto.TrackSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.music.data.master.entity.TrackBinding;
import yurykorzun.art.universe.music.data.master.entity.TrackCategory;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.ArtistRelatedLookupService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final TrackBindingRepository bindingsRepository;
    private final TrackCategoryRepository trackCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ArtistService artistService;
    private final RelationService relationService;
    private final ArtistRelatedLookupService lookupService;

    public TrackServiceImpl(
        TrackRepository trackRepository,
        TrackBindingRepository bindingsRepository,
        TrackCategoryRepository trackCategoryRepository,
        CategoryRepository categoryRepository,
        ArtistService artistService,
        RelationService relationService,
        EntityManager entityManager
    ) {
        this.trackRepository = trackRepository;
        this.bindingsRepository = bindingsRepository;
        this.trackCategoryRepository = trackCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.artistService = artistService;
        this.relationService = relationService;
        this.lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
    }

    @Override
    public Page<TrackDto> findTracks(String search, Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            return trackRepository.findTracks(search, pageable)
                    .map(this::mapToDto);
        } else {
            return trackRepository.findTracks(search, categoryId, pageable)
                    .map(this::mapToDto);
        }
    }

    @Override
    public Page<TrackWithCategoriesDto> findTracksWithCategories(String search, Long categoryId, Pageable pageable) {
        return trackRepository.findTracksWithCategories(search, categoryId, pageable)
                .map(this::mapToTrackWithCategories);
    }

    @Override
    public TrackDto getTrack(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Track", id));
        return mapToDto(track);
    }

    @Override
    public TrackWithCategoriesDto getTrackWithCategories(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Track", id));
        return mapToTrackWithCategories(track);
    }

    @Override
    @Transactional
    public TrackDto saveTrack(TrackSaveRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        Track track;
        if (request.getId() != null) {
            // Update existing track
            track = trackRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomEntityNotFoundException("Track", request.getId()));
            track.setName(request.getName());
            if (request.getPrimaryArtistId() != null) {
                track.setPrimaryArtistId(request.getPrimaryArtistId());
            }
        } else {
            // Create new track — primaryArtistId is required
            if (request.getPrimaryArtistId() == null) {
                throw new IllegalArgumentException("primaryArtistId is required when creating a new track");
            }
            track = Track.builder()
                    .name(request.getName())
                    .primaryArtistId(request.getPrimaryArtistId())
                    .origin(origin)
                    .approvalStatus(approvalStatus)
                    .build();
        }

        Track savedTrack = trackRepository.save(track);
        return mapToDto(savedTrack);
    }

    @Override
    @Transactional
    public boolean deleteTrack(Long id) {
        if (trackRepository.existsById(id)) {
            trackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void bindToCategory(Long trackId, Long categoryId, Origin origin, MasterApprovalStatus approvalStatus) {
        if (!trackRepository.existsById(trackId)) {
            throw new CustomEntityNotFoundException("Track", trackId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }
        if (trackCategoryRepository.existsByTrackIdAndCategoryId(trackId, categoryId)) {
            throw new IllegalArgumentException("Relation between track " + trackId + " and category " + categoryId + " already exists");
        }

        TrackCategory relation = TrackCategory.builder()
            .trackId(trackId)
            .categoryId(categoryId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();
        trackCategoryRepository.save(relation);
    }

    @Override
    @Transactional
    public void unbindFromCategory(Long trackId, Long categoryId) {
        if (!trackRepository.existsById(trackId)) {
            throw new CustomEntityNotFoundException("Track", trackId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }

        TrackCategory relation = trackCategoryRepository.findByTrackIdAndCategoryId(trackId, categoryId)
            .orElseThrow(() -> new CustomEntityNotFoundException("Relation between track " + trackId + " and category " + categoryId + " not found"));
        trackCategoryRepository.delete(relation);
    }

    private TrackDto mapToDto(Track track) {
        return TrackDto.builder()
                .id(track.getId())
                .name(track.getName())
                .primaryArtistId(track.getPrimaryArtistId())
                .build();
    }

    private TrackWithCategoriesDto mapToTrackWithCategories(Track track) {
        List<CategoryDto> categories = List.of();
        if (track.getCategoryRelations() != null) {
            categories = track.getCategoryRelations().stream()
                    .map(relation -> CategoryDto.builder()
                            .id(relation.getCategory().getId())
                            .name(relation.getCategory().getName())
                            .build())
                    .toList();
        }

        return TrackWithCategoriesDto.builder()
                .id(track.getId())
                .name(track.getName())
                .primaryArtistId(track.getPrimaryArtistId())
                .categories(categories)
                .build();
    }

    @Override
    public List<BoundEntityProjection> findBoundTracks(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundTracksForDataSource(dataSource, externalIds);
    }

    @Override
    public BoundEntityProjection findTrack(DataSource dataSource, Long externalId) {
        List<BoundEntityProjection> results = bindingsRepository.findBoundTracksForDataSource(dataSource, List.of(externalId));
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        Track track = trackRepository.findById(request.getMasterId())
            .orElseThrow(() -> new CustomEntityNotFoundException("Track", request.getMasterId()));
        Long masterArtistId = track.getPrimaryArtistId();

        Optional<TrackBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);

        if (existingBinding.isPresent()) {
            TrackBinding binding = existingBinding.get();
            if (!binding.getMasterId().equals(track.getId())) {
                binding.setMasterId(track.getId());
                bindingsRepository.save(binding);
            }
        } else {
            TrackBinding binding = TrackBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .masterId(track.getId())
                .origin(origin)
                .approvalStatus(approvalStatus)
                .build();
            bindingsRepository.save(binding);
        }

        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId, MasterEntityType.TRACK, track.getId(), null, origin, approvalStatus);

        return bindingsRepository.findBoundTracksForDataSource(dataSource, List.of(externalId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Track binding not found after creation"));
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request, Origin origin, MasterApprovalStatus approvalStatus) {
        Long masterArtistId = resolveMasterArtistId(request.getMasterPrimaryArtistId());

        Optional<Track> existingTrack = trackRepository.findByNameAndPrimaryArtistId(request.getEntityName(), masterArtistId);
        if (existingTrack.isPresent()) {
            throw new IllegalArgumentException(
                String.format("Track with name '%s' for artist ID %d already exists",
                    request.getEntityName(), masterArtistId)
            );
        }

        Optional<TrackBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        if (existingBinding.isPresent()) {
            throw new IllegalStateException(
                String.format("Track binding for external ID %d from %s already exists",
                    externalId, dataSource)
            );
        }

        Track track = Track.builder()
            .name(request.getEntityName())
            .primaryArtistId(masterArtistId)
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();

        Track savedTrack = trackRepository.save(track);

        TrackBinding binding = TrackBinding.builder()
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(savedTrack.getId())
            .origin(origin)
            .approvalStatus(approvalStatus)
            .build();

        bindingsRepository.save(binding);

        relationService.createInternalRelation(
            MasterEntityType.ARTIST, masterArtistId, MasterEntityType.TRACK, savedTrack.getId(), null, origin, approvalStatus);

        return bindingsRepository.findBoundTracksForDataSource(dataSource, List.of(externalId))
            .stream().findFirst()
            .orElseThrow(() -> new CustomEntityNotFoundException("Track binding not found after creation"));
    }

    @Override
    @Transactional
    public boolean unbindTrack(DataSource dataSource, Long externalId) {
        Optional<TrackBinding> binding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);

        if (binding.isPresent()) {
            bindingsRepository.delete(binding.get());
            return true;
        }

        return false;
    }

    @Override
    public List<LookupResultDTO> lookupTracks(ArtistRelatedLookupRequestDTO request) {
        return lookupService.lookup(request);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchLookupResponseDTO batchLookupTracks(ArtistRelatedBatchLookupRequestDTO request) {
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
