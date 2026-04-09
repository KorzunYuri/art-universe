package yurykorzun.art.universe.music.data.master.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CategoryDagDTO {
    List<CategoryDagNodeDTO> nodes;
    List<CategoryDagEdgeDTO> edges;
}
