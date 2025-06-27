package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySaveRequestDTO {
    
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private Long dimensionId;
    
    private Long parentId;
}
