package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import java.time.Duration;

/**
 * Final classification of a failed LLM response, combining generic status-based
 * type with provider-extracted metadata (retry-after, quota granularity).
 * Consumed by {@link LlmClientCircuitBreaker} and the registry to decide how
 * long to keep the client in COOLING state.
 *
 * @param failureType   one of {@link LlmFailureType}
 * @param retryAfter    server-provided hint (null if not given)
 * @param quotaKind     {@link QuotaKind} if this was a quota hit, else UNKNOWN
 * @param detailMessage human-readable reason (null if none)
 */
public record FailureHint(
    LlmFailureType failureType,
    Duration retryAfter,
    QuotaKind quotaKind,
    String detailMessage
) {
    public static FailureHint of(LlmFailureType type, ProviderHint providerHint) {
        return new FailureHint(
            type,
            providerHint.retryAfter(),
            providerHint.quotaKind(),
            providerHint.detailMessage()
        );
    }
}
