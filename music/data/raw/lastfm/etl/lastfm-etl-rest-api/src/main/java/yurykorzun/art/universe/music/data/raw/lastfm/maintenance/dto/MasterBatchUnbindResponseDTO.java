package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch unbind response from music-data-master. Mirrors the DTO that exists on target side
 * TODO extract to music-data-master-api module
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterBatchUnbindResponseDTO {
    
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
