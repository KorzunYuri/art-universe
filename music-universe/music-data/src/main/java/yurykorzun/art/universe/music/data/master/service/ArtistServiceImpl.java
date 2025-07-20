package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.ArtistBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistBindingRepository bindingsRepository;
    private final EntityManager entityManager;

    public ArtistServiceImpl(
        ArtistRepository artistRepository,
        ArtistBindingRepository bindingsRepository,
        EntityManager entityManager
    ) {
        this.artistRepository = artistRepository;
        this.bindingsRepository = bindingsRepository;
        this.entityManager = entityManager;
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
            if (!binding.getMasterId().equals(artist.getId())) {
                binding.setMasterId(artist.getId());
                bindingsRepository.save(binding);
            }
        } else {
            // Create new binding
            ArtistBinding binding = ArtistBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .masterId(artist.getId())
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
            binding.setMasterId(savedArtist.getId());
            bindingsRepository.save(binding);
        } else {
            // Create new binding
            ArtistBinding binding = ArtistBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .masterId(savedArtist.getId())
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
    
    @Override
    @Transactional(readOnly = true)
    public ArtistBatchLookupResponseDTO batchLookupArtists(ArtistBatchLookupRequestDTO request) {
        if (request.getSearchTerms() == null || request.getSearchTerms().isEmpty()) {
            return ArtistBatchLookupResponseDTO.builder().build();
        }
        
        // Apply default limit if null
        int actualLimit = request.getLimit() != null ? request.getLimit() : 20;
        
        // Filter and prepare search terms
        List<String> searchTerms = request.getSearchTerms().stream()
            .filter(term -> term != null && !term.trim().isEmpty())
            .map(String::trim)
            .collect(Collectors.toList());
        
        if (searchTerms.isEmpty()) {
            return ArtistBatchLookupResponseDTO.builder().build();
        }
        
        // Dynamically build SQL query with UNION ALL
        StringBuilder sqlBuilder = new StringBuilder();
        
        for (int i = 0; i < searchTerms.size(); i++) {
            if (i > 0) {
                sqlBuilder.append("\nUNION ALL\n");
            }
            
            sqlBuilder.append("""
                SELECT * FROM (
                    SELECT a.id, a.name, a.created_at, a.updated_at, ? as search_term
                    FROM artist a
                    WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', ?, '%'))
                    ORDER BY a.name ASC
                    LIMIT ?
                ) AS result
            """);
        }
        
        // Create query and set parameters
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        
        int paramIndex = 1;
        for (String searchTerm : searchTerms) {
            query.setParameter(paramIndex++, searchTerm); // For search_term column
            query.setParameter(paramIndex++, searchTerm); // For WHERE clause
            query.setParameter(paramIndex++, actualLimit); // For LIMIT
        }
        
        // Execute the query
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        // Process results into the response DTO
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        
        for (Object[] row : results) {
            Long id = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String searchTerm = (String) row[4];
            
            LookupResultDTO dto = LookupResultDTO.builder()
                .id(id)
                .name(name)
                .build();
            
            resultMap.computeIfAbsent(searchTerm, k -> new ArrayList<>()).add(dto);
        }
        
        return ArtistBatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
    }
}
