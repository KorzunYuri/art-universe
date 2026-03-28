package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.openai.OpenAiLlmClient;

@Configuration
public class LlmClientConfig {

    @Bean
    public LlmClient llmClient(
        @Value("${semantic.llm.api-key:}") String apiKey,
        @Value("${semantic.llm.base-url:https://api.openai.com/v1}") String baseUrl,
        @Value("${semantic.llm.model:gpt-4o}") String model
    ) {
        return new OpenAiLlmClient(apiKey, baseUrl, model);
    }
}
