package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Analyzer for Google Gemini API. Gemini returns structured error details
 * with a {@code retryDelay} hint and a {@code quotaId} that distinguishes
 * per-minute from per-day quotas.
 * <p>
 * Expected response shape (may be wrapped in an array):
 * <pre>
 * {
 *   "error": {
 *     "code": 429,
 *     "status": "RESOURCE_EXHAUSTED",
 *     "details": [
 *       { "@type": ".../QuotaFailure", "violations": [{"quotaId": "...PerDay..."}] },
 *       { "@type": ".../RetryInfo",    "retryDelay": "23s" }
 *     ]
 *   }
 * }
 * </pre>
 */
@Component
public class GeminiProviderErrorAnalyzer implements ProviderErrorAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GeminiProviderErrorAnalyzer.class);
    private static final String RETRY_INFO_TYPE = "RetryInfo";
    private static final String QUOTA_FAILURE_TYPE = "QuotaFailure";

    private final ObjectMapper objectMapper;

    public GeminiProviderErrorAnalyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String provider() {
        return "gemini";
    }

    @Override
    public ProviderHint extractHints(int httpStatus, Map<String, List<String>> headers, String body) {
        if (body == null || body.isBlank()) {
            return ProviderHint.EMPTY;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode errorNode = root.isArray() && !root.isEmpty()
                ? root.get(0).path("error")
                : root.path("error");
            if (errorNode.isMissingNode() || errorNode.isNull()) {
                return ProviderHint.EMPTY;
            }

            Duration retryAfter = null;
            QuotaKind quotaKind = QuotaKind.UNKNOWN;

            for (JsonNode detail : errorNode.path("details")) {
                String type = detail.path("@type").asText("");
                if (type.contains(RETRY_INFO_TYPE) && retryAfter == null) {
                    retryAfter = parseRetryDelay(detail.path("retryDelay").asText(null));
                } else if (type.contains(QUOTA_FAILURE_TYPE) && quotaKind == QuotaKind.UNKNOWN) {
                    quotaKind = inferQuotaKind(detail);
                }
            }

            String message = errorNode.path("message").asText(null);
            return new ProviderHint(retryAfter, quotaKind, message);
        } catch (Exception e) {
            log.debug("Failed to parse Gemini error body, returning empty hint: {}", e.getMessage());
            return ProviderHint.EMPTY;
        }
    }

    private Duration parseRetryDelay(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        // Gemini format: "23s" or "23.0694s"
        String trimmed = raw.trim();
        if (!trimmed.endsWith("s")) {
            return null;
        }
        try {
            double seconds = Double.parseDouble(trimmed.substring(0, trimmed.length() - 1));
            return Duration.ofMillis(Math.round(seconds * 1000));
        } catch (NumberFormatException e) {
            log.debug("Unparseable retryDelay value: '{}'", raw);
            return null;
        }
    }

    private QuotaKind inferQuotaKind(JsonNode quotaFailure) {
        for (JsonNode violation : quotaFailure.path("violations")) {
            String quotaId = violation.path("quotaId").asText("");
            if (quotaId.contains("PerDay")) {
                return QuotaKind.PER_DAY;
            }
            if (quotaId.contains("PerMinute")) {
                return QuotaKind.PER_MINUTE;
            }
        }
        return QuotaKind.UNKNOWN;
    }
}
