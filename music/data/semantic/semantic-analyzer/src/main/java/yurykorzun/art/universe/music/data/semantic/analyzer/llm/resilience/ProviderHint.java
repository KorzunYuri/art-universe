package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import java.time.Duration;

/**
 * Provider-specific facts extracted from an error response, before the
 * client-level classifier folds them together with configured ban patterns.
 *
 * @param retryAfter    server-provided retry hint (null if absent)
 * @param quotaKind     granularity of the quota that was hit (PER_MINUTE / PER_DAY / UNKNOWN)
 * @param detailMessage human-readable reason extracted from the response body (null if none)
 */
public record ProviderHint(
    Duration retryAfter,
    QuotaKind quotaKind,
    String detailMessage
) {
    public static final ProviderHint EMPTY = new ProviderHint(null, QuotaKind.UNKNOWN, null);
}
