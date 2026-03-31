package yurykorzun.art.universe.music.data.semantic.analyzer.llm.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmRequest;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmResponse;

public class OpenAiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String defaultModel;
    private final Double temperature;

    public OpenAiLlmClient(String apiKey, String baseUrl, String defaultModel, Double temperature) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
        this.objectMapper = new ObjectMapper();
        this.defaultModel = defaultModel;
        this.temperature = temperature;
    }

    @Override
    public LlmResponse analyze(LlmRequest request) {
        try {
            String model = request.getModel() != null ? request.getModel() : defaultModel;

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

            String responseBody = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode usage = responseJson.path("usage");

            return LlmResponse.builder()
                .content(responseJson.path("choices").path(0).path("message").path("content").asText())
                .provider("openai")
                .model(model)
                .promptTokens(usage.path("prompt_tokens").asInt())
                .completionTokens(usage.path("completion_tokens").asInt())
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            return LlmResponse.builder()
                .provider("openai")
                .success(false)
                .errorMessage(e.getMessage())
                .build();
        }
    }
}
