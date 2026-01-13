package yurykorzun.art.universe.common.domain.dto.lookup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for batch lookup requests that use standard LookupRequestDTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BaseBatchLookupRequestDTO extends BatchLookupRequestDTO<LookupRequestDTO> {
    // This class provides a concrete implementation of BatchLookupRequestDTO for LookupRequestDTO
}
