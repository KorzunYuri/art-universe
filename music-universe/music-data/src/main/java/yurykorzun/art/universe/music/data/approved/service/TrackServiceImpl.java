package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TrackBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
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

    public TrackServiceImpl(
        TrackRepository trackRepository,
        TrackBindingRepository bindingsRepository,
        ArtistService artistService
    ) {
        this.trackRepository = trackRepository;
        this.bindingsRepository = bindingsRepository;
        this.artistService = artistService;
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
    public BoundEntityProjection bindTrack(DataSource dataSource, Long externalId, TrackBindingRequestDTO request) {
        // 1. Check that artist is bound
        BoundEntityProjection artistBinding = artistService.findArtist(dataSource, request.getArtistExternalId());
        if (artistBinding == null) {
            throw new IllegalStateException(
                String.format("Artist with external ID %d from %s must be bound before binding track", 
                    request.getArtistExternalId(), dataSource)
            );
        }
        
        Long artistId = artistBinding.getReferenceId();
        
        // 2. Create or find the track
        Track track = trackRepository.findByNameAndPrimaryArtistId(request.getName(), artistId)
            .orElseGet(() -> {
                Track newTrack = Track.builder()
                    .name(request.getName())
                    .primaryArtistId(artistId)
                    .build();
                return trackRepository.save(newTrack);
            });
        
        // 3. Check if binding already exists
        Optional<TrackBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding if needed
            TrackBinding binding = existingBinding.get();
            if (!binding.getReferenceId().equals(track.getId())) {
                binding.setReferenceId(track.getId());
                bindingsRepository.save(binding);
            }
            
            // Return the updated binding
            return bindingsRepository.findBoundTracksForDataSource(dataSource, List.of(externalId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Binding not found after update"));
        } else {
            // Create new binding
            TrackBinding binding = TrackBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(track.getId())
                .build();
            
            bindingsRepository.save(binding);
            
            // Return the created binding
            return bindingsRepository.findBoundTracksForDataSource(dataSource, List.of(externalId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Binding not found after creation"));
        }
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
