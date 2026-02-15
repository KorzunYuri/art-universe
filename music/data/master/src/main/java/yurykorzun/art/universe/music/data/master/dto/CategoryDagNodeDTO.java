package yurykorzun.art.universe.music.data.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryDagNodeDTO {

    Long id;
    String name;

    @JsonProperty("isRoot")
    boolean isRoot;

    int childrenCount;
    int artistsCount;
    int tracksCount;
}
