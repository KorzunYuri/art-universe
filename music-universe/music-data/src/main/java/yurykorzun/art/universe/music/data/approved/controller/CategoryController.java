package yurykorzun.art.universe.music.data.approved.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<Page<CategoryHierarchyProjection>>> searchCategories(
        @RequestParam(required = false) String query,
        Pageable pageable
    ) {
        try {
            Page<CategoryHierarchyProjection> categories = categoryService.searchCategories(query, pageable);
            return ResponseWrapper.success(categories);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to search categories: %s", e.getMessage()));
        }
    }

    @GetMapping("/lookup")
    public ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> lookupCategories(
        @RequestParam String name,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            List<LookupResultDTO> categories = limit != null
                ? categoryService.lookupCategories(name, limit)
                : categoryService.lookupCategories(name);
            return ResponseWrapper.success(categories);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to lookup categories: %s", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<CategoryHierarchyProjection>> saveCategory(
        @Valid @RequestBody CategorySaveRequestDTO request
    ) {
        try {
            CategoryHierarchyProjection savedCategory = categoryService.saveCategory(request);
            return ResponseWrapper.success(savedCategory);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to save category: %s", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Boolean>> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                return ResponseWrapper.success(true);
            } else {
                return ResponseWrapper.failure("Category not found with id: " + id);
            }
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to delete category: %s", e.getMessage()));
        }
    }
}
