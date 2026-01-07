package yurykorzun.art.universe.music.data.master.dto.binding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TODO extract to music-data-master-api module to provide lightweight dependency for consumers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUnbindResponseDTO {
    
    /**
     * List of external IDs that were successfully unbound
     */
    private List<Long> successfullyUnbound;
    
    /**
     * List of external IDs that were not found (no binding existed)
     */
    private List<Long> notFound;
    
    /**
     * Total number of external IDs processed
     */
    private int totalProcessed;
    
    /**
     * Number of bindings successfully removed
     */
    private int successCount;
    
    /**
     * Number of external IDs that had no existing binding
     */
    private int notFoundCount;
}
