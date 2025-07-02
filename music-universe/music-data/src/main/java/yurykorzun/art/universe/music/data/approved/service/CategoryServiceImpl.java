package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.Category;
import yurykorzun.art.universe.music.data.approved.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.approved.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.DimensionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryBindingRepository categoryBindingRepository;
    private final DimensionRepository dimensionRepository;

    public CategoryServiceImpl(
        CategoryRepository categoryRepository,
        CategoryBindingRepository categoryBindingRepository,
        DimensionRepository dimensionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryBindingRepository = categoryBindingRepository;
        this.dimensionRepository = dimensionRepository;
    }

    @Override
    public Page<CategoryHierarchyProjection> searchCategories(String search, Pageable pageable) {
        return categoryRepository.searchCategories(search, pageable);
    }

    @Override
    public List<LookupResultDTO> lookupCategories(String name, Integer limit) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        
        // Apply default limit if null
        int actualLimit = limit != null ? limit : 20;
        
        return categoryRepository.findByNameContainingIgnoreCase(name.trim(), actualLimit)
            .stream()
            .map(category -> LookupResultDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build())
            .collect(Collectors.toList());
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
    public BoundEntityProjection findCategory(DataSource dataSource, Long externalId) {
        return categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Override
    @Transactional
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, CategoryBindToExistingRequestDTO request) {
        // Validate that the category exists
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Check if binding already exists
        Optional<CategoryBinding> existingBinding = categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding if needed
            CategoryBinding binding = existingBinding.get();
            if (!binding.getReferenceId().equals(category.getId())) {
                binding.setReferenceId(category.getId());
                categoryBindingRepository.save(binding);
            }
        } else {
            // Create new binding
            CategoryBinding binding = CategoryBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(category.getId())
                .build();
            
            categoryBindingRepository.save(binding);
        }
        
        // Return the binding information
        return categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Override
    @Transactional
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, CategoryCreateAndBindRequestDTO request) {
        // Create new category
        Category category = Category.builder()
            .name(request.getName())
            .build();
        
        Category savedCategory = categoryRepository.save(category);
        
        // Check if binding already exists
        Optional<CategoryBinding> existingBinding = categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId);
        
        if (existingBinding.isPresent()) {
            // Update existing binding
            CategoryBinding binding = existingBinding.get();
            binding.setReferenceId(savedCategory.getId());
            categoryBindingRepository.save(binding);
        } else {
            // Create new binding
            CategoryBinding binding = CategoryBinding.builder()
                .dataSource(dataSource)
                .externalId(externalId)
                .referenceId(savedCategory.getId())
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
