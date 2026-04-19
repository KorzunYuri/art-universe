package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "semantic")
public class AnalysisModeProperties {

    /**
     * Maps analysis mode name to an ordered list of LLM client names.
     * The first healthy client in the list is used; remaining entries are fallbacks.
     * <p>
     * Example:
     * <pre>
     * full_extraction:
     *   - extraction
     *   - creative-gemini
     * creative_categorization:
     *   - creative-groq
     *   - creative-gemini
     * </pre>
     */
    private Map<String, List<String>> modes;
}
