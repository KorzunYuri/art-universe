package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/search")
    public Page<CategoryHierarchyProjection> searchCategories(
        @RequestParam(required = false) String query,
        Pageable pageable
    ) {
        try {
            return categoryService.searchCategories(query, pageable);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to search categories: %s", e.getMessage()), e);
        }
    }

    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupCategories(
        @RequestParam String name,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            return limit != null
                ? categoryService.lookupCategories(name, limit)
                : categoryService.lookupCategories(name);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to lookup categories: %s", e.getMessage()), e);
        }
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupCategories(
        @Valid @RequestBody BatchLookupRequestDTO request
    ) {
        try {
            return categoryService.batchLookupCategories(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to batch lookup categories: %s", e.getMessage()), e);
        }
    }

    @PostMapping
    public CategoryHierarchyProjection saveCategory(
        @Valid @RequestBody CategorySaveRequestDTO request
    ) {
        try {
            return categoryService.saveCategory(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to save category: %s", e.getMessage()), e);
        }
    }

    @DeleteMapping("/{id}")
    public boolean deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (!deleted) {
                throw new EntityNotFoundException("Category", id);
            }
            return true;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to delete category: %s", e.getMessage()), e);
        }
    }

    @GetMapping("/bound/{dataSource}")
    public List<BoundEntityProjection> findBoundCategories(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        try {
            return categoryService.findBoundCategories(dataSource, externalIds);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to get bound categories: %s", e.getMessage()), e);
        }
    }

    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityBindToExistingRequestDTO request
    ) {
        try {
            return categoryService.bindToExisting(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to bind category to existing: %s", e.getMessage()), e);
        }
    }

    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityCreateAndBindRequestDTO request
    ) {
        try {
            return categoryService.createAndBind(dataSource, externalId, request);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to create and bind category: %s", e.getMessage()), e);
        }
    }

    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindCategory(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        try {
            return categoryService.unbindCategory(dataSource, externalId);
        } catch (Exception e) {
            throw new EntityBindingException(String.format("Failed to unbind category: %s", e.getMessage()), e);
        }
    }
}
