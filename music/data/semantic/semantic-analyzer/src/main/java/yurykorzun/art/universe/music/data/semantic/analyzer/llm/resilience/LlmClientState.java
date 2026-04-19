package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

public enum LlmClientState {
    /** Normal operation. */
    HEALTHY,
    /** Temporarily unavailable (rate-limited). Auto-probes after cooldown expires. */
    COOLING,
    /** Banned or manually disabled. No auto-recovery. */
    DISABLED
}
