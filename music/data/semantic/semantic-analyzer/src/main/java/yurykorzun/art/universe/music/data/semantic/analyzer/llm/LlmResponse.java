package yurykorzun.art.universe.music.data.semantic.analyzer.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String errorMessage;
}
