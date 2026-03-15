package yurykorzun.art.universe.common.config.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.common.config.client.dto.BulkRegisterRequest;
import yurykorzun.art.universe.common.config.client.dto.BulkRegisterResponse;
import yurykorzun.art.universe.common.config.client.dto.PropertyResponse;
import yurykorzun.art.universe.common.config.client.dto.RegisterPropertyRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ConfigServiceClient(ConfigClientProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
            .baseUrl(properties.getServiceUrl())
            .build();
    }

    /**
     * Bulk-registers properties with the config service (idempotent).
     * Returns the list of properties with their current persisted values.
     */
    public List<PropertyResponse> registerAll(List<ConfigurableProperty> configurableProperties) {
        List<RegisterPropertyRequest> requests = configurableProperties.stream()
            .map(this::toRequest)
            .collect(Collectors.toList());

        BulkRegisterResponse response = restClient.post()
            .uri("/api/v1/config/properties/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new BulkRegisterRequest(requests))
            .retrieve()
            .body(BulkRegisterResponse.class);

        return response == null ? List.of() : response.getProperties();
    }

    /**
     * Fetches all currently registered properties.
     */
    public List<PropertyResponse> fetchAll() {
        PropertyResponse[] all = restClient.get()
            .uri("/api/v1/config/properties")
            .retrieve()
            .body(PropertyResponse[].class);

        return all == null ? List.of() : Arrays.asList(all);
    }

    private RegisterPropertyRequest toRequest(ConfigurableProperty property) {
        String constraintsJson = null;
        if (property.getConstraints() != null) {
            constraintsJson = serializeConstraints(property.getConstraints());
        }
        return new RegisterPropertyRequest(
            property.getKey(),
            property.getPropertyType(),
            property.getDefaultValue(),
            property.getDescription(),
            constraintsJson
        );
    }

    private String serializeConstraints(PropertyConstraints constraints) {
        Map<String, Object> map = new HashMap<>();
        if (constraints.getMin() != null) {
            map.put("min", constraints.getMin());
        }
        if (constraints.getMax() != null) {
            map.put("max", constraints.getMax());
        }
        if (constraints.getAllowedValues() != null) {
            map.put("allowedValues", constraints.getAllowedValues());
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize constraints for property", e);
        }
    }
}
