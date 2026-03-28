package yurykorzun.art.universe.music.data.master.category.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class TicketIntakeClient {

    private static final Logger log = LoggerFactory.getLogger(TicketIntakeClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketIntakeClient(@Value("${trigger.ticket-intake.url}") String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    public void submitBatch(List<ObjectNode> tickets) {
        try {
            ArrayNode batch = objectMapper.createArrayNode();
            tickets.forEach(batch::add);

            String response = webClient.post()
                .uri("/api/v1/tickets/batch")
                .bodyValue(batch.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode result = objectMapper.readTree(response);
            int accepted = result.path("accepted").size();
            int duplicates = result.path("rejected").path("duplicate").size();
            log.info("Batch submitted: {} accepted, {} duplicates", accepted, duplicates);
        } catch (Exception e) {
            log.error("Failed to submit ticket batch", e);
        }
    }
}
