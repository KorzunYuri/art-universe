package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.CategoryCategory;

import java.util.List;

@Repository
public interface CategoryCategoryRepository extends JpaRepository<CategoryCategory, Long> {

    /**
     * Find all parent categories for a given category
     */
    @Query("SELECT cc.sourceCategoryId FROM category_category cc WHERE cc.targetCategoryId = :categoryId")
    List<Long> findParentIds(@Param("categoryId") Long categoryId);

    /**
     * Find all child categories for a given category
     */
    @Query("SELECT cc.targetCategoryId FROM category_category cc WHERE cc.sourceCategoryId = :categoryId")
    List<Long> findChildIds(@Param("categoryId") Long categoryId);

    /**
     * Check if a relationship exists between two categories
     */
    boolean existsBySourceCategoryIdAndTargetCategoryId(Long sourceCategoryId, Long targetCategoryId);

    List<CategoryCategory> findByTargetCategoryId(Long id);
}
