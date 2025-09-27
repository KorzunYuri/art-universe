package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch unbind request to music-data-master. Mirrors the DTO that exists on target side
 * TODO extract to music-data-master-api module
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterBatchUnbindRequestDTO {
    
    @NotNull(message = "External IDs list cannot be null")
    @NotEmpty(message = "External IDs list cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 external IDs allowed per batch")
    private List<Long> externalIds;
}
