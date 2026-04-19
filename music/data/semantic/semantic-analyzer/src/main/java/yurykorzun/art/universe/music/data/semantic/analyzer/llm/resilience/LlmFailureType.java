package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

public enum LlmFailureType {
    /** 429 — provider asks us to slow down. Triggers cooling + fallback. */
    RATE_LIMITED,
    /** Detected ban pattern in response. Disables client until manual re-enable. */
    BANNED,
    /** Non-retriable client error (400, 401, etc.). */
    CLIENT_ERROR,
    /** Server-side error (5xx). May be transient. */
    SERVER_ERROR,
    /** Timeout, connection refused, etc. */
    NETWORK_ERROR
}
