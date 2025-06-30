package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.Artist;
import yurykorzun.art.universe.music.data.approved.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistBindingRepository bindingsRepository;

    public ArtistServiceImpl(
        ArtistRepository artistRepository,
        ArtistBindingRepository bindingsRepository
    ) {
        this.artistRepository = artistRepository;
        this.bindingsRepository = bindingsRepository;
    }

    @Override
    public List<BoundEntityProjection> findBoundArtists(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundArtistsForDataSource(dataSource, externalIds);
    }
    
    @Override
    public BoundEntityProjection findArtist(DataSource dataSource, Long externalId) {
        return bindingsRepository.findBoundArtistForDataSource(dataSource, externalId);
    }
    
    @Override
    @Transactional
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistBindToExistingRequestDTO request) {
        // Validate that the artist exists
        Artist artist = artistRepository.findById(request.getArtistId())
            .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + request.getArtistId()));
        
        // Check if binding already exists
        Optional<ArtistBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding if needed
            ArtistBinding binding = existingBinding.get();
            if (!binding.getReferenceId().equals(artist.getId())) {
                binding.setReferenceId(artist.getId());
                bindingsRepository.save(binding);
            }
        } else {
            // Create new binding
            ArtistBinding binding = ArtistBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(artist.getId())
                .build();
            
            bindingsRepository.save(binding);
        }
        
        // Return the binding information
        return bindingsRepository.findBoundArtistForDataSource(dataSource, externalId);
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistCreateAndBindRequestDTO request) {
        artistRepository.findByName(request.getName())
            .ifPresent(artist -> {
                throw new IllegalArgumentException(String.format("Artist with name %s already exists", artist.getName()));
            });

        // Create new artist
        Artist artist = Artist.builder()
            .name(request.getName())
            .build();
        
        Artist savedArtist = artistRepository.save(artist);
        
        // Check if binding already exists
        Optional<ArtistBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding
            ArtistBinding binding = existingBinding.get();
            binding.setReferenceId(savedArtist.getId());
            bindingsRepository.save(binding);
        } else {
            // Create new binding
            ArtistBinding binding = ArtistBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(savedArtist.getId())
                .build();
            
            bindingsRepository.save(binding);
        }
        
        // Return the binding information
        return bindingsRepository.findBoundArtistForDataSource(dataSource, externalId);
    }    
    @Override
    @Transactional
    public boolean unbindArtist(DataSource dataSource, Long externalId) {
        Optional<ArtistBinding> binding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (binding.isPresent()) {
            bindingsRepository.delete(binding.get());
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<LookupResultDTO> searchArtistsByName(String search, Integer limit) {
        if (search == null || search.trim().isEmpty()) {
            return List.of();
        }
        
        // Apply default limit if null
        int actualLimit = limit != null ? limit : 20;
        
        return artistRepository.findByNameContainingIgnoreCase(search.trim(), actualLimit)
            .stream()
            .map(artist -> LookupResultDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .build())
            .collect(Collectors.toList());
    }
}
