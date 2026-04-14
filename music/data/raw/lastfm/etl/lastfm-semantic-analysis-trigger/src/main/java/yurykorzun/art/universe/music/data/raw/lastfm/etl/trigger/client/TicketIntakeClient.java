package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.model.TicketRequest;

import java.util.List;

@Component
public class TicketIntakeClient {

    private static final Logger log = LoggerFactory.getLogger(TicketIntakeClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TicketIntakeClient(
        @Value("${trigger.ticket-intake.url:http://localhost:7095}") String baseUrl,
        ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.objectMapper = objectMapper;
    }

    public void submitBatch(List<TicketRequest> tickets) {
        try {
            String body = objectMapper.writeValueAsString(tickets);

            String response = restClient.post()
                .uri("/api/v1/tickets/batch")
                .body(body)
                .retrieve()
                .body(String.class);

            JsonNode result = objectMapper.readTree(response);
            int accepted = result.path("accepted").size();
            int duplicates = result.path("rejected").path("duplicate").size();
            log.info("Batch submitted: {} accepted, {} duplicates", accepted, duplicates);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ticket batch", e);
        } catch (Exception e) {
            log.error("Failed to submit ticket batch", e);
        }
    }
}
