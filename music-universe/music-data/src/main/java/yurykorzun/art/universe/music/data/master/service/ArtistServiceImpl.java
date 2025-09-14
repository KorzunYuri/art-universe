package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistDto;
import yurykorzun.art.universe.music.data.master.dto.ArtistSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.MasterEntityLookupService;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistBindingRepository bindingsRepository;
    private final ArtistCategoryRepository artistCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final MasterEntityLookupService lookupService;

    public ArtistServiceImpl(
        ArtistRepository artistRepository,
        ArtistBindingRepository bindingsRepository,
        ArtistCategoryRepository artistCategoryRepository,
        CategoryRepository categoryRepository,
        EntityManager entityManager
    ) {
        this.artistRepository = artistRepository;
        this.bindingsRepository = bindingsRepository;
        this.artistCategoryRepository = artistCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.lookupService = new MasterEntityLookupService(entityManager, MasterEntityType.ARTIST);
    }

    @Override
    public Page<ArtistDto> findArtists(String search, Pageable pageable) {
        return artistRepository.findArtists(search, pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<ArtistWithCategoriesDto> findArtistsWithCategories(String search, Pageable pageable) {
        return artistRepository.findArtistsWithCategories(search, pageable)
                .map(this::mapToArtistWithCategories);
    }

    @Override
    public ArtistDto getArtist(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Artist", id));
        return mapToDto(artist);
    }

    @Override
    public ArtistWithCategoriesDto getArtistWithCategories(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Artist", id));
        return mapToArtistWithCategories(artist);
    }

    @Override
    @Transactional
    public ArtistDto saveArtist(ArtistSaveRequestDTO request) {
        Artist artist;
        if (request.getId() != null) {
            // Update existing artist
            artist = artistRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomEntityNotFoundException("Artist", request.getId()));
            artist.setName(request.getName());
        } else {
            // Create new artist
            artist = Artist.builder()
                    .name(request.getName())
                    .build();
        }

        Artist savedArtist = artistRepository.save(artist);
        return mapToDto(savedArtist);
    }

    @Override
    @Transactional
    public boolean deleteArtist(Long id) {
        if (artistRepository.existsById(id)) {
            artistRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ArtistDto mapToDto(Artist artist) {
        return ArtistDto.builder()
                .id(artist.getId())
                .name(artist.getName())
                .build();
    }

    private ArtistWithCategoriesDto mapToArtistWithCategories(Artist artist) {
        List<CategoryDto> categories = List.of();
        if (artist.getCategoryRelations() != null) {
            categories = artist.getCategoryRelations().stream()
                    .map(relation -> CategoryDto.builder()
                            .id(relation.getCategory().getId())
                            .name(relation.getCategory().getName())
                            .build())
                    .toList();
        }

        return ArtistWithCategoriesDto.builder()
                .id(artist.getId())
                .name(artist.getName())
                .categories(categories)
                .build();
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
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, EntityBindToExistingRequestDTO request) {
        // Validate that the artist exists
        Artist artist = artistRepository.findById(request.getMasterId())
            .orElseThrow(() -> new CustomEntityNotFoundException("Artist", request.getMasterId()));
        
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
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, EntityCreateAndBindRequestDTO request) {
        artistRepository.findByName(request.getEntityName())
            .ifPresent(artist -> {
                throw new IllegalArgumentException(String.format("Artist with name %s already exists", artist.getName()));
            });

        // Create new artist
        Artist artist = Artist.builder()
            .name(request.getEntityName())
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
    public List<LookupResultDTO> lookupArtists(LookupRequestDTO request) {
        return lookupService.lookup(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BatchLookupResponseDTO batchLookupArtists(BaseBatchLookupRequestDTO request) {
        return lookupService.batchLookup(request);
    }

    @Override
    @Transactional
    public void bindToCategory(Long artistId, Long categoryId) {
        // Validate artist exists
        if (!artistRepository.existsById(artistId)) {
            throw new CustomEntityNotFoundException("Artist", artistId);
        }
        
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }
        
        // Check if relation already exists
        if (artistCategoryRepository.existsByArtistIdAndCategoryId(artistId, categoryId)) {
            throw new IllegalArgumentException("Relation between artist " + artistId + " and category " + categoryId + " already exists");
        }
        
        // Create relation
        ArtistCategory relation = ArtistCategory.builder()
            .artistId(artistId)
            .categoryId(categoryId)
            .build();
        artistCategoryRepository.save(relation);
    }

    @Override
    @Transactional
    public void unbindFromCategory(Long artistId, Long categoryId) {
        // Validate artist exists
        if (!artistRepository.existsById(artistId)) {
            throw new CustomEntityNotFoundException("Artist", artistId);
        }
        
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }
        
        // Find and delete relation
        ArtistCategory relation = artistCategoryRepository.findByArtistIdAndCategoryId(artistId, categoryId)
            .orElseThrow(() -> new CustomEntityNotFoundException("Relation between artist " + artistId + " and category " + categoryId + " not found"));
        
        artistCategoryRepository.delete(relation);
    }
}
