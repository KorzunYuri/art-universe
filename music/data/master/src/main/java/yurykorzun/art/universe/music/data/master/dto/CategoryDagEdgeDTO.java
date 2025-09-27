package yurykorzun.art.universe.music.data.master.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryDagEdgeDTO {
    Long source; // parent category id
    Long target; // child category id
}
