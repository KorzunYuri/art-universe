package yurykorzun.art.universe.music.data.master.dto.lookup;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified DTO for batch lookup requests across all entity types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLookupRequestDTO {

    @NotEmpty(message = "At least one search term is required")
    private List<String> searchTerms;
    
    private Integer limit;
}
