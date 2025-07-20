package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.DimensionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryBindingRepository categoryBindingRepository;
    private final DimensionRepository dimensionRepository;
    private final EntityManager entityManager;

    public CategoryServiceImpl(
        CategoryRepository categoryRepository,
        CategoryBindingRepository categoryBindingRepository,
        DimensionRepository dimensionRepository,
        EntityManager entityManager
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryBindingRepository = categoryBindingRepository;
        this.dimensionRepository = dimensionRepository;
        this.entityManager = entityManager;
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
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, CategoryCreateAndBindRequestDTO request) {
        // Check if category with the same name already exists
        categoryRepository.findByName(request.getName())
            .ifPresent(category -> {
                throw new IllegalArgumentException(String.format("Category with name %s already exists", category.getName()));
            });

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
    
    @Override
    @Transactional(readOnly = true)
    public CategoryBatchLookupResponseDTO batchLookupCategories(CategoryBatchLookupRequestDTO request) {
        if (request.getNames() == null || request.getNames().isEmpty()) {
            return CategoryBatchLookupResponseDTO.builder().build();
        }
        
        // Apply default limit if null
        int actualLimit = request.getLimit() != null ? request.getLimit() : 20;
        
        // Filter and prepare search terms
        List<String> searchTerms = request.getNames().stream()
            .filter(term -> term != null && !term.trim().isEmpty())
            .map(String::trim)
            .toList();
        
        if (searchTerms.isEmpty()) {
            return CategoryBatchLookupResponseDTO.builder().build();
        }
        
        // Dynamically build SQL query with UNION ALL
        StringBuilder sqlBuilder = new StringBuilder();
        
        for (int i = 0; i < searchTerms.size(); i++) {
            if (i > 0) {
                sqlBuilder.append("\nUNION ALL\n");
            }
            
            sqlBuilder.append("""
                SELECT * FROM (
                    SELECT c.id, c.name, c.created_at, c.updated_at, ? as search_term
                    FROM category c
                    WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', ?, '%'))
                    ORDER BY c.name ASC
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
        
        return CategoryBatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
    }
}
