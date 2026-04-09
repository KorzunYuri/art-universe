package yurykorzun.art.universe.music.data.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeDTO {
    private Long id;
    private String name;
    private String reverseName;

    @JsonProperty("isSymmetrical")
    private boolean isSymmetrical;

    @JsonProperty("isSystem")
    private boolean isSystem;
}
