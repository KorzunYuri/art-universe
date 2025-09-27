package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRelationDTO {
    
    @NotNull(message = "Source category ID is required")
    private Long sourceId;
    
    @NotNull(message = "Target category ID is required")
    private Long targetId;
}
