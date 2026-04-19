package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;

/**
 * Result of {@code LlmClientRegistry.acquireClient}: the chosen client together
 * with its name (needed for reporting success/failure back to the registry).
 */
public record ClientSelection(LlmClient client, String clientName) {
}
