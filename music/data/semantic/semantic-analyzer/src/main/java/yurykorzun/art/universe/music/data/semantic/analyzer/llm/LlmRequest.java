package yurykorzun.art.universe.music.data.semantic.analyzer.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {

    private String systemPrompt;
    private String userPrompt;
    private String model;
    private boolean jsonMode;
}
