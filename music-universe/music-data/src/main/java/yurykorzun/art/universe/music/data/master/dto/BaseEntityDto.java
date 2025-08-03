package yurykorzun.art.universe.music.data.master.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class BaseEntityDto {
    private Long id;
    private String name;
}
