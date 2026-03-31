package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "semantic")
public class AnalysisModeProperties {

    /**
     * Maps analysis mode name to LLM client name.
     * Example: full_extraction -> extraction, creative_categorization -> creative
     */
    private Map<String, String> modes;
}
