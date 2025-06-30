package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
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
    public BoundEntityProjection bindArtist(DataSource dataSource, Long externalId, ArtistBindingRequestDTO request) {
        // Create or find the artist
        Artist artist = artistRepository.findByName(request.getName())
            .orElseGet(() -> {
                Artist newArtist = Artist.builder()
                    .name(request.getName())
                    .build();
                return artistRepository.save(newArtist);
            });
        
        // Check if binding already exists
        Optional<ArtistBinding> existingBinding = bindingsRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding if needed
            ArtistBinding binding = existingBinding.get();
            if (!binding.getReferenceId().equals(artist.getId())) {
                binding.setReferenceId(artist.getId());
                bindingsRepository.save(binding);
            }
            
            // Return the updated binding
            return bindingsRepository.findBoundArtistsForDataSource(dataSource, List.of(externalId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Binding not found after update"));
        } else {
            // Create new binding
            ArtistBinding binding = ArtistBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(artist.getId())
                .build();
            
            bindingsRepository.save(binding);
            
            // Return the created binding
            return bindingsRepository.findBoundArtistsForDataSource(dataSource, List.of(externalId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Binding not found after creation"));
        }
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
