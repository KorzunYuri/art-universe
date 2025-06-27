package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.entity.Category;
import yurykorzun.art.universe.music.data.approved.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.approved.repository.DimensionRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final DimensionRepository dimensionRepository;

    public CategoryServiceImpl(
        CategoryRepository categoryRepository,
        DimensionRepository dimensionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.dimensionRepository = dimensionRepository;
    }

    @Override
    public Page<CategoryHierarchyProjection> searchCategories(String search, Pageable pageable) {
        return categoryRepository.searchCategories(search, pageable);
    }

    @Override
    @Transactional
    public CategoryHierarchyProjection saveCategory(CategorySaveRequestDTO request) {
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
}
