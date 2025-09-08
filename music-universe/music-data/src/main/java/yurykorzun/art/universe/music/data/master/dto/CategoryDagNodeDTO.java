package yurykorzun.art.universe.music.data.master.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryDagNodeDTO {
    Long id;
    String name;
    boolean isRoot;
}
