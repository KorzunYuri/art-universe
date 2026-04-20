package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Per-client error classifier. Combines two sources of signal:
 * <ul>
 *   <li>A per-provider {@link ProviderErrorAnalyzer} that extracts
 *       retry/quota metadata (e.g. {@code Retry-After} header for OpenAI,
 *       {@code details[].retryDelay} for Gemini)</li>
 *   <li>A per-client list of ban patterns from YAML, matched against status
 *       and body. A pattern matches when all non-null conditions hold.</li>
 * </ul>
 * Ban patterns are evaluated first. If none match, we fall back to generic
 * status-based classification (429 → RATE_LIMITED, 4xx → CLIENT_ERROR,
 * 5xx → SERVER_ERROR, other → NETWORK_ERROR).
 */
public final class LlmFailureClassifier {

    private final String clientName;
    private final ProviderErrorAnalyzer providerAnalyzer;
    private final List<CompiledBanPattern> banPatterns;

    public LlmFailureClassifier(
        String clientName,
        ProviderErrorAnalyzer providerAnalyzer,
        List<CompiledBanPattern> banPatterns
    ) {
        this.clientName = clientName;
        this.providerAnalyzer = providerAnalyzer;
        this.banPatterns = List.copyOf(banPatterns);
    }

    public FailureHint classify(int httpStatus, Map<String, List<String>> headers, String body) {
        ProviderHint providerHint = providerAnalyzer.extractHints(httpStatus, headers, body);
        LlmFailureType type = determineType(httpStatus, body);
        return FailureHint.of(type, providerHint);
    }

    private LlmFailureType determineType(int httpStatus, String body) {
        for (CompiledBanPattern pattern : banPatterns) {
            if (pattern.matches(httpStatus, body)) {
                return LlmFailureType.BANNED;
            }
        }
        if (httpStatus == 429) {
            return LlmFailureType.RATE_LIMITED;
        }
        if (httpStatus >= 400 && httpStatus < 500) {
            return LlmFailureType.CLIENT_ERROR;
        }
        if (httpStatus >= 500) {
            return LlmFailureType.SERVER_ERROR;
        }
        return LlmFailureType.NETWORK_ERROR;
    }

    public String getClientName() {
        return clientName;
    }

    /**
     * Pre-compiled ban pattern. Either {@code status} or {@code bodyPattern}
     * (or both) must be set; a rule with both null is rejected at build time.
     */
    public static final class CompiledBanPattern {
        private final Integer status;
        private final Pattern bodyPattern;

        public CompiledBanPattern(Integer status, Pattern bodyPattern) {
            if (status == null && bodyPattern == null) {
                throw new IllegalArgumentException(
                    "Ban pattern must have at least one of status or body-pattern set");
            }
            this.status = status;
            this.bodyPattern = bodyPattern;
        }

        public boolean matches(int httpStatus, String errorBody) {
            if (status != null && status != httpStatus) {
                return false;
            }
            if (bodyPattern != null) {
                if (errorBody == null) {
                    return false;
                }
                return bodyPattern.matcher(errorBody).find();
            }
            return true;
        }
    }
}
