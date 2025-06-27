package yurykorzun.art.universe.music.data.approved.dto;

/**
 * Projection interface for category hierarchy information.
 */
public interface CategoryHierarchyProjection {
    Long getId();
    String getName();
    Long getDimensionId();
    Long getEffectiveDimensionId();
    Long getParentId();
    Integer getHierarchyLevel();
    String getDimensionName();
    String getEffectiveDimensionName();
    String getParentName();
}
