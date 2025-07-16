package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.approved.dto.TrackBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.TrackCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;
import yurykorzun.art.universe.music.data.approved.entity.Track;
import yurykorzun.art.universe.music.data.approved.entity.TrackBinding;
import yurykorzun.art.universe.music.data.approved.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final TrackBindingRepository bindingsRepository;
    private final ArtistService artistService;
    private final RelationService relationService;

    public TrackServiceImpl(
        TrackRepository trackRepository,
        TrackBindingRepository bindingsRepository,
        ArtistService artistService,
        RelationService relationService
    ) {
        this.trackRepository = trackRepository;
        this.bindingsRepository = bindingsRepository;
        this.artistService = artistService;
        this.relationService = relationService;
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
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, TrackBindToExistingRequestDTO request) {
        // 1. Check that artist is bound
        BoundEntityProjection artistBinding = artistService.findArtist(dataSource, request.getArtistExternalId());
        if (artistBinding == null) {
            throw new IllegalStateException(
                String.format("Artist with external ID %d from %s must be bound before binding track", 
                    request.getArtistExternalId(), dataSource)
            );
        }
        
        // 2. Validate that the track exists
        Track track = trackRepository.findById(request.getTrackId())
            .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + request.getTrackId()));
        
        // 3. Create or update track binding
        Optional<TrackBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding if needed
            TrackBinding binding = existingBinding.get();
            if (!binding.getReferenceId().equals(track.getId())) {
                binding.setReferenceId(track.getId());
                bindingsRepository.save(binding);
            }
        } else {
            // Create new binding
            TrackBinding binding = TrackBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(track.getId())
                .build();
            
            bindingsRepository.save(binding);
        }
        
        // 4. Create relation between artist and track
        Long artistId = artistBinding.getReferenceId();
        Long relationId = relationService.createInternalRelation(
            EntityType.ARTIST, artistId, EntityType.TRACK, track.getId());
        
        // 5. Bind external relation and return result
        return bindExternalRelationAndGetResult(dataSource, externalId, request.getArtistExternalId());
    }
    
    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, TrackCreateAndBindRequestDTO request) {
        // 1. Check that artist is bound
        BoundEntityProjection artistBinding = artistService.findArtist(dataSource, request.getArtistExternalId());
        if (artistBinding == null) {
            throw new IllegalStateException(
                String.format("Artist with external ID %d from %s must be bound before binding track", 
                    request.getArtistExternalId(), dataSource)
            );
        }
        
        Long artistId = artistBinding.getReferenceId();
        
        // 2. Check if track already exists
        Optional<Track> existingTrack = trackRepository.findByNameAndPrimaryArtistId(request.getName(), artistId);
        if (existingTrack.isPresent()) {
            throw new IllegalArgumentException(
                String.format("Track with name '%s' for artist ID %d already exists", 
                    request.getName(), artistId)
            );
        }
        
        // 3. Check that track binding doesn't exist
        Optional<TrackBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        if (existingBinding.isPresent()) {
            throw new IllegalStateException(
                String.format("Track binding for external ID %d from %s already exists", 
                    externalId, dataSource)
            );
        }
        
        // 4. Create new track
        Track track = Track.builder()
            .name(request.getName())
            .primaryArtistId(artistId)
            .build();
        
        Track savedTrack = trackRepository.save(track);
        
        // 5. Create track binding
        TrackBinding binding = TrackBinding.builder()
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(savedTrack.getId())
            .build();
        
        bindingsRepository.save(binding);
        
        // 6. Create relation between artist and track
        Long relationId = relationService.createInternalRelation(
            EntityType.ARTIST, artistId, EntityType.TRACK, savedTrack.getId());
        
        // 7. Bind external relation and return result
        return bindExternalRelationAndGetResult(dataSource, externalId, request.getArtistExternalId());
    }
    
    /**
     * Helper method to bind external relation and return the track binding result
     * 
     * @param dataSource Data source
     * @param externalId External track ID
     * @param artistExternalId External artist ID
     * @return Bound entity projection
     */
    private BoundEntityProjection bindExternalRelationAndGetResult(
        DataSource dataSource, 
        Long externalId, 
        Long artistExternalId
    ) {
        // 1. Bind external relation
        RelationBindingDTO binding = relationService.bindExternalRelation(
            dataSource, 
            EntityType.ARTIST, artistExternalId, 
            EntityType.TRACK, externalId);
        
        // 2. Return the track binding
        return bindingsRepository.findBoundTracksForDataSource(dataSource, List.of(externalId))
            .stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Track binding not found after creation"));
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
}
