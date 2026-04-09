package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to save category field values. Category relations are managed separately.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySaveRequestDTO {
    
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;
}
