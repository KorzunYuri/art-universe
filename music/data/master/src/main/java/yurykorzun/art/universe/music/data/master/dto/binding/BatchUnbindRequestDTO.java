package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BatchUnbindRequestDTO {
    
    @NotNull(message = "External IDs list cannot be null")
    @NotEmpty(message = "External IDs list cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 external IDs allowed per batch")
    private List<Long> externalIds;
}
