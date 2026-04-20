package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import java.util.List;
import java.util.Map;

/**
 * Provider-specific error response interpretation. Each implementation knows
 * how to read retry hints and quota metadata from its API's error envelope
 * (OpenAI's {@code Retry-After} header, Gemini's {@code details[].retryDelay},
 * etc). Stateless — one bean per provider.
 * <p>
 * The generic failure type classification (BANNED / RATE_LIMITED / CLIENT_ERROR /
 * SERVER_ERROR) is handled by {@link LlmFailureClassifier}; this interface is
 * only responsible for extracting provider-shaped details.
 */
public interface ProviderErrorAnalyzer {

    /**
     * The provider name this analyzer handles (matches {@code semantic.llm.clients.*.provider}).
     */
    String provider();

    /**
     * Extracts retry hints and quota metadata from an error response.
     * Implementations must tolerate malformed bodies / missing headers and
     * return {@link ProviderHint#EMPTY} when nothing useful can be read.
     */
    ProviderHint extractHints(int httpStatus, Map<String, List<String>> headers, String body);
}
