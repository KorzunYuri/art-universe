package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindToExistingRequestDTO {
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
