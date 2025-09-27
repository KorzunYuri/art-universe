package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySaveWithParentsRequestDTO {
    
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private List<Long> parents;
}
