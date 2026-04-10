package yurykorzun.art.universe.music.data.semantic.analyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReprocessingRequestDto(
    @NotBlank @JsonProperty("from_version") String fromVersion,
    @NotBlank @JsonProperty("to_version") String toVersion,
    @Min(1) @JsonProperty("batch_size") Integer batchSize
) {
    public int batchSizeOrDefault(int defaultValue) {
        return batchSize != null ? batchSize : defaultValue;
    }
}
