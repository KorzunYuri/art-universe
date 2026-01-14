package yurykorzun.art.universe.common.domain.dto.lookup;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Abstract base class for batch lookup requests
 * @param <T> The type of lookup request contained in this batch
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BatchLookupRequestDTO<T extends LookupRequestDTO> {
    @NotNull(message = "Search requests are required")
    private List<T> searchRequests;
    private Integer limit;
}
