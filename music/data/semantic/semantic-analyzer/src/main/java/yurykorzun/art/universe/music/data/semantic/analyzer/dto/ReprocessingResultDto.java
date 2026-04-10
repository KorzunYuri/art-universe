package yurykorzun.art.universe.music.data.semantic.analyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReprocessingResultDto(
    @JsonProperty("from_version") String fromVersion,
    @JsonProperty("to_version") String toVersion,
    @JsonProperty("tickets_created") int ticketsCreated,
    @JsonProperty("proposals_superseded") int proposalsSuperseded
) {}
