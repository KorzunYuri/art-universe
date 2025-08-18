package yurykorzun.art.universe.common.dto.lookup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified DTO for batch lookup responses across all entity types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLookupResponseDTO {
    
    @Builder.Default
    private Map<String, List<LookupResultDTO>> results = new HashMap<>();
}
