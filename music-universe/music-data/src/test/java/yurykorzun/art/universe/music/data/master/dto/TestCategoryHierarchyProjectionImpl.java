package yurykorzun.art.universe.music.data.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TestCategoryHierarchyProjectionImpl implements CategoryHierarchyProjection {
    private Long id;
    private String name;
    private Long dimensionId;
    private Long effectiveDimensionId;
    private Long parentId;
    private Integer hierarchyLevel;
    private String dimensionName;
    private String effectiveDimensionName;
    private String parentName;
}
