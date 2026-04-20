package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ProviderErrorAnalyzer;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ProviderHint;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.QuotaKind;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Analyzer for OpenAI and OpenAI-clone APIs. Reads the {@code Retry-After}
 * response header (seconds or HTTP-date per RFC 7231).
 */
@Component
public class OpenAiProviderErrorAnalyzer implements ProviderErrorAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProviderErrorAnalyzer.class);
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    @Override
    public String provider() {
        return "openai";
    }

    @Override
    public ProviderHint extractHints(int httpStatus, Map<String, List<String>> headers, String body) {
        Duration retryAfter = parseRetryAfter(headers);
        return new ProviderHint(retryAfter, QuotaKind.UNKNOWN, null);
    }

    private Duration parseRetryAfter(Map<String, List<String>> headers) {
        if (headers == null) {
            return null;
        }
        List<String> values = findHeader(headers, RETRY_AFTER_HEADER);
        if (values == null || values.isEmpty()) {
            return null;
        }
        String raw = values.get(0);
        try {
            long seconds = Long.parseLong(raw.trim());
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException ignore) {
            log.debug("Non-numeric Retry-After header: '{}'", raw);
            return null;
        }
    }

    private static List<String> findHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
