package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBatchLookupRequestDTO {
    
    @NotEmpty(message = "Category names list cannot be empty")
    private List<String> names;
    
    private Integer limit;
}
