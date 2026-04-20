package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.provider;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ProviderErrorAnalyzer;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ProviderHint;

import java.util.List;
import java.util.Map;

/**
 * Analyzer for Groq API. Groq's 429 responses don't carry a structured retry
 * hint (no Retry-After header, no nested details), and bans are detected via
 * the per-client {@code ban-patterns} configured in {@code llm.yml}. So this
 * analyzer is passive — it returns {@link ProviderHint#EMPTY} and lets the
 * classifier do its job from status + body + configured patterns alone.
 */
@Component
public class GroqProviderErrorAnalyzer implements ProviderErrorAnalyzer {

    @Override
    public String provider() {
        return "groq";
    }

    @Override
    public ProviderHint extractHints(int httpStatus, Map<String, List<String>> headers, String body) {
        return ProviderHint.EMPTY;
    }
}
