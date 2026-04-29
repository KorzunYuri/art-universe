package yurykorzun.art.universe.music.data.master.dto.proposal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisRequestStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequestDto {

    private UUID id;
    private UUID ticketId;
    private String analysisVersion;
    private DataSource dataSource;
    private AttributeTargetType subjectType;
    private Long subjectId;
    private AnalysisRequestStatus status;
    private String llmProvider;
    private String llmModel;
    private Integer promptTokens;
    private Integer completionTokens;
    private String errorMessage;
    private Instant createdAt;
    private Instant completedAt;
}
