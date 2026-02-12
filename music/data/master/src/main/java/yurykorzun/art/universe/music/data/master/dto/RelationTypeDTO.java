package yurykorzun.art.universe.music.data.master.dto;

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
    private boolean isSymmetrical;
}
