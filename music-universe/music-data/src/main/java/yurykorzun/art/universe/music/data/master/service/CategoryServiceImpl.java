package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.DimensionRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.BaseLookupService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryBindingRepository categoryBindingRepository;
    private final DimensionRepository dimensionRepository;
    private final BaseLookupService lookupService;

    public CategoryServiceImpl(
        CategoryRepository categoryRepository,
        CategoryBindingRepository categoryBindingRepository,
        DimensionRepository dimensionRepository,
        EntityManager entityManager
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryBindingRepository = categoryBindingRepository;
        this.dimensionRepository = dimensionRepository;
        this.lookupService = new BaseLookupService(entityManager, EntityType.CATEGORY);
    }

    @Override
    public Page<CategoryHierarchyProjection> findCategories(String search, Pageable pageable) {
        return categoryRepository.findCategories(search, pageable);
    }

    @Override
    public CategoryHierarchyProjection getCategory(Long id) {
        return categoryRepository.findByIdWithHierarchy(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
    }

    @Override
    public List<LookupResultDTO> lookupCategories(LookupRequestDTO request) {
        return lookupService.lookup(request);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchLookupResponseDTO batchLookupCategories(BaseBatchLookupRequestDTO request) {
        return lookupService.batchLookup(request);
    }

    @Override
    @Transactional
    public CategoryHierarchyProjection saveCategory(CategorySaveRequestDTO request) {
        // Validate that category cannot be parent of itself
        if (request.getId() != null && request.getParentId() != null && request.getId().equals(request.getParentId())) {
            throw new IllegalArgumentException("Category cannot be parent of itself");
        }
        
        // Validate dimension if provided
        if (request.getDimensionId() != null) {
            dimensionRepository.findById(request.getDimensionId())
                .orElseThrow(() -> new EntityNotFoundException("Dimension not found with id: " + request.getDimensionId()));
        }
        
        // Validate parent if provided
        if (request.getParentId() != null) {
            categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + request.getParentId()));
        }
        
        Category category;
        if (request.getId() != null) {
            // Update existing category
            category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getId()));
            
            category.setName(request.getName());
            category.setDimensionId(request.getDimensionId());
            category.setParentId(request.getParentId());
        } else {
            // Create new category
            category = Category.builder()
                .name(request.getName())
                .dimensionId(request.getDimensionId())
                .parentId(request.getParentId())
                .build();
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        // Return the saved category with hierarchy information
        return categoryRepository.findByIdWithHierarchy(savedCategory.getId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found after save"));
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<BoundEntityProjection> findBoundCategories(DataSource dataSource, List<Long> externalIds) {
        return categoryBindingRepository.findBoundCategoriesForDataSource(dataSource, externalIds);
    }

    @Override
    public BoundEntityProjection findBoundCategory(DataSource dataSource, Long externalId) {
        return categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Override
    @Transactional
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, EntityBindToExistingRequestDTO request) {
        // Validate that the category exists
        Category category = categoryRepository.findById(request.getMasterId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getMasterId()));
        
        // Check if binding already exists
        Optional<CategoryBinding> existingBinding = categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding if needed
            CategoryBinding binding = existingBinding.get();
            if (!binding.getMasterId().equals(category.getId())) {
                binding.setMasterId(category.getId());
                categoryBindingRepository.save(binding);
            }
        } else {
            // Create new binding
            CategoryBinding binding = CategoryBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .masterId(category.getId())
                .build();
            
            categoryBindingRepository.save(binding);
        }
        
        // Return the binding information
        return categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, EntityCreateAndBindRequestDTO request) {
        // Check if category with the same name already exists
        categoryRepository.findByName(request.getEntityName())
            .ifPresent(category -> {
                throw new IllegalArgumentException(String.format("Category with name %s already exists", category.getName()));
            });

        // Create new category
        Category category = Category.builder()
            .name(request.getEntityName())
            .build();
        
        Category savedCategory = categoryRepository.save(category);
        
        // Check if binding already exists
        Optional<CategoryBinding> existingBinding = categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding
            CategoryBinding binding = existingBinding.get();
            binding.setMasterId(savedCategory.getId());
            categoryBindingRepository.save(binding);
        } else {
            // Create new binding
            CategoryBinding binding = CategoryBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .masterId(savedCategory.getId())
                .build();
            
            categoryBindingRepository.save(binding);
        }
        
        // Return the binding information
        return categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Override
    @Transactional
    public boolean unbindCategory(DataSource dataSource, Long externalId) {
        Optional<CategoryBinding> binding = categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (binding.isPresent()) {
            categoryBindingRepository.delete(binding.get());
            return true;
        }
        
        return false;
    }
}
