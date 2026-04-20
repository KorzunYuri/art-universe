package yurykorzun.art.universe.music.data.semantic.analyzer.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {

    private String content;
    private String provider;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private boolean success;
    private boolean rateLimited;
    private int httpStatus;
    private String rawErrorBody;
    private Map<String, List<String>> rawErrorHeaders;
    private String errorMessage;
}
