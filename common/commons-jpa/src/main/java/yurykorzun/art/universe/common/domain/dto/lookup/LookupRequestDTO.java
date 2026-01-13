package yurykorzun.art.universe.common.domain.dto.lookup;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LookupRequestDTO {
    @NotNull(message = "Search term is required")
    private String search;
    private Integer limit;
}
