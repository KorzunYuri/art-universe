package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.CategoryWithParentsDto;
import yurykorzun.art.universe.common.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveWithParentsRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagNodeDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagEdgeDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.CategoryCategory;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryCategoryRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.MasterEntityLookupService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryBindingRepository categoryBindingRepository;
    private final CategoryCategoryRepository categoryCategoryRepository;
    private final MasterEntityLookupService lookupService;

    public CategoryServiceImpl(
        CategoryRepository categoryRepository,
        CategoryBindingRepository categoryBindingRepository,
        CategoryCategoryRepository categoryCategoryRepository,
        EntityManager entityManager
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryBindingRepository = categoryBindingRepository;
        this.categoryCategoryRepository = categoryCategoryRepository;
        this.lookupService = new MasterEntityLookupService(entityManager, MasterEntityType.CATEGORY);
    }

    @Override
    public Page<CategoryDto> findCategories(String search, Pageable pageable) {
        return categoryRepository.findCategories(search, pageable)
            .map(this::mapToDto);
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
            .id(category.getId())
            .name(category.getName())
            .build();
    }

    @Override
    public List<CategoryWithParentsDto> findCategoriesWithParents(String search) {
        List<Category> categories = categoryRepository.findCategoriesWithParentsEntities(search);
        return categories.stream()
            .map(this::mapToCategoryWithParents)
            .toList();
    }

    private CategoryWithParentsDto mapToCategoryWithParents(Category category) {
        List<CategoryDto> parents = List.of();
        if (category.getParentRelations() != null) {
            parents = category.getParentRelations().stream()
                .map(relation -> CategoryDto.builder()
                    .id(relation.getSourceCategory().getId())
                    .name(relation.getSourceCategory().getName())
                    .build())
                .toList();
        }
        
        return CategoryWithParentsDto.builder()
            .id(category.getId())
            .name(category.getName())
            .parents(parents)
            .build();
    }

    @Override
    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
        return mapToDto(category);
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
    public CategoryDto saveCategory(CategorySaveRequestDTO request) {
        Category category;
        if (request.getId() != null) {
            // Update existing category
            category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getId()));
            
            category.setName(request.getName());
        } else {
            // Create new category
            category = Category.builder()
                .name(request.getName())
                .build();
        }
        
        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryWithParentsDto saveCategoryWithParents(CategorySaveWithParentsRequestDTO request) {
        Category category;
        if (request.getId() != null) {
            // Update existing category
            category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getId()));
            
            category.setName(request.getName());
        } else {
            // Create new category
            category = Category.builder()
                .name(request.getName())
                .build();
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        // Update parent relationships
        if (request.getId() != null) {
            // Remove existing parent relationships
            categoryCategoryRepository.deleteAll(categoryCategoryRepository.findByTargetCategoryId(savedCategory.getId()));
        }
        
        // Add new parent relationships
        if (request.getParents() != null) {
            for (Long parentId : request.getParents()) {
                if (parentId != null) {
                    CategoryCategory relation = CategoryCategory.builder()
                        .sourceCategoryId(parentId)
                        .targetCategoryId(savedCategory.getId())
                        .build();
                    categoryCategoryRepository.save(relation);
                }
            }
        }
        
        // Return the saved category with parent information
        Category categoryWithParents = categoryRepository.findById(savedCategory.getId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found after save"));
        
        return mapToCategoryWithParents(categoryWithParents);
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            // Find all parents and children
            List<Long> parents = categoryCategoryRepository.findParentIds(id);
            List<Long> children = categoryCategoryRepository.findChildIds(id);
            
            // Connect each child to each parent (smart cascade deletion)
            for (Long childId : children) {
                for (Long parentId : parents) {
                    if (!categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(parentId, childId)) {
                        CategoryCategory relation = CategoryCategory.builder()
                            .sourceCategoryId(parentId)
                            .targetCategoryId(childId)
                            .build();
                        categoryCategoryRepository.save(relation);
                    }
                }
            }
            
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

    @Override
    @Transactional(readOnly = true)
    public CategoryDagDTO getCategoryDag() {
        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryCategory> allRelations = categoryCategoryRepository.findAll();
        
        List<CategoryDagNodeDTO> nodes = allCategories.stream()
            .map(category -> {
                boolean isRoot = allRelations.stream()
                    .noneMatch(rel -> rel.getTargetCategoryId().equals(category.getId()));
                return CategoryDagNodeDTO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .isRoot(isRoot)
                    .build();
            })
            .toList();
        
        List<CategoryDagEdgeDTO> edges = allRelations.stream()
            .map(relation -> CategoryDagEdgeDTO.builder()
                .source(relation.getSourceCategoryId())
                .target(relation.getTargetCategoryId())
                .build())
            .toList();
        
        return CategoryDagDTO.builder()
            .nodes(nodes)
            .edges(edges)
            .build();
    }
}
