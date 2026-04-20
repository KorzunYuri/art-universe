package yurykorzun.art.universe.music.data.semantic.analyzer.llm.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmRequest;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmResponse;

import java.time.Duration;

/**
 * HTTP transport for OpenAI-compatible chat completion APIs. Used for OpenAI,
 * Google Gemini (via OpenAI-compat shim), Groq, and anything else that speaks
 * the {@code /chat/completions} wire format. Provider-specific error
 * interpretation (retry hints, quota granularity) lives in a separate
 * {@code ProviderErrorAnalyzer} strategy — this class only captures the raw
 * status, body and headers and hands them up.
 */
public class OpenAiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String provider;
    private final String defaultModel;
    private final Double temperature;
    private final AdaptiveRateLimiter rateLimiter;

    public OpenAiLlmClient(
        ObjectMapper objectMapper,
        String provider,
        String apiKey,
        String baseUrl,
        String defaultModel,
        Double temperature,
        Duration connectTimeout,
        Duration readTimeout,
        AdaptiveRateLimiter rateLimiter
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) connectTimeout.toMillis());
        requestFactory.setReadTimeout((int) readTimeout.toMillis());
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .requestFactory(requestFactory)
            .build();
        this.objectMapper = objectMapper;
        this.provider = provider;
        this.defaultModel = defaultModel;
        this.temperature = temperature;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public LlmResponse analyze(LlmRequest request) {
        String model = request.getModel() != null ? request.getModel() : defaultModel;
        try {
            rateLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return LlmResponse.builder()
                .provider(provider)
                .model(model)
                .success(false)
                .errorMessage("Rate limiter sleep interrupted")
                .build();
        }

        try {
            String body = buildRequestBody(request, model);
            String responseBody = restClient.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(String.class);

            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode usage = responseJson.path("usage");

            rateLimiter.recordSuccess();
            return LlmResponse.builder()
                .content(responseJson.path("choices").path(0).path("message").path("content").asText())
                .provider(provider)
                .model(model)
                .promptTokens(usage.path("prompt_tokens").asInt())
                .completionTokens(usage.path("completion_tokens").asInt())
                .success(true)
                .build();

        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            String errorBody = e.getResponseBodyAsString();
            if (status == 429) {
                rateLimiter.record429();
                log.warn("{} API rate-limited (429): {}", provider, errorBody);
            } else {
                log.error("{} API call failed: status={}, body={}", provider, status, errorBody, e);
            }
            return LlmResponse.builder()
                .provider(provider)
                .model(model)
                .success(false)
                .rateLimited(status == 429)
                .httpStatus(status)
                .rawErrorBody(errorBody)
                .rawErrorHeaders(e.getResponseHeaders())
                .errorMessage(status + ": " + errorBody)
                .build();
        } catch (RestClientException | JsonProcessingException e) {
            log.error("{} API call failed", provider, e);
            return LlmResponse.builder()
                .provider(provider)
                .model(model)
                .success(false)
                .errorMessage(e.getMessage())
                .build();
        }
    }

    private String buildRequestBody(LlmRequest request, String model) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);

        ArrayNode messages = body.putArray("messages");
        if (request.getSystemPrompt() != null) {
            ObjectNode sysMsg = messages.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", request.getSystemPrompt());
        }
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", request.getUserPrompt());

        if (temperature != null) {
            body.put("temperature", temperature);
        }

        if (request.isJsonMode()) {
            ObjectNode responseFormat = body.putObject("response_format");
            responseFormat.put("type", "json_object");
        }

        return body.toString();
    }
}
