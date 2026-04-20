package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

/**
 * Granularity of a quota that has been hit. Influences how long the circuit
 * breaker cools the client: per-minute is transient (seconds); per-day is
 * exhausted until calendar rollover.
 */
public enum QuotaKind {
    UNKNOWN,
    PER_MINUTE,
    PER_DAY
}
