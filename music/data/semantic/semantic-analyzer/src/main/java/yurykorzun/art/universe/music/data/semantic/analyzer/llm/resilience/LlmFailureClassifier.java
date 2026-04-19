package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Classifies LLM API error responses into actionable failure types, using
 * a per-client list of configured ban patterns.
 * <p>
 * Each {@link CompiledBanPattern} matches when both (non-null) conditions
 * hold: HTTP status and body regex. Missing fields act as wildcards, so a
 * status-only rule (body=null) triggers on any body, and a body-only rule
 * (status=null) triggers on any status.
 * <p>
 * Ban patterns are evaluated first; if none matches, we fall back to the
 * generic status-based classification (429 → RATE_LIMITED, 4xx → CLIENT_ERROR,
 * 5xx → SERVER_ERROR, other → NETWORK_ERROR).
 */
public final class LlmFailureClassifier {

    private final String clientName;
    private final List<CompiledBanPattern> banPatterns;

    public LlmFailureClassifier(String clientName, List<CompiledBanPattern> banPatterns) {
        this.clientName = clientName;
        this.banPatterns = List.copyOf(banPatterns);
    }

    public LlmFailureType classify(int httpStatus, String errorBody) {
        for (CompiledBanPattern pattern : banPatterns) {
            if (pattern.matches(httpStatus, errorBody)) {
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
