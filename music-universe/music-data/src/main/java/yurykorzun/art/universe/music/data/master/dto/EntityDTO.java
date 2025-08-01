package yurykorzun.art.universe.music.data.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for entity information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityDTO {
    private Long id;
    private String name;
    private EntityType entityType;
}
