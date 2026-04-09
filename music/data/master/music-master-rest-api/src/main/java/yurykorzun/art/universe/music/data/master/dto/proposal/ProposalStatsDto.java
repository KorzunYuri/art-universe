package yurykorzun.art.universe.music.data.master.dto.proposal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalStatsDto {

    private long totalCount;
    private Map<String, Long> byResolution;
    private Map<String, Long> byType;
}
