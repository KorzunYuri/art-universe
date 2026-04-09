package yurykorzun.art.universe.music.data.master.dto.proposal;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResolveRequestDTO {

    @NotEmpty(message = "At least one proposal ID is required")
    private List<Long> ids;
}
