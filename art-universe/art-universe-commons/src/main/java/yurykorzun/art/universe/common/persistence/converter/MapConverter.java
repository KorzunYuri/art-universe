package yurykorzun.art.universe.common.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@Converter(autoApply = true)
public class MapConverter implements AttributeConverter<Map<String, String>, String>{

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> map) {
        String json = null;
        try {
            json = mapper.writeValueAsString(map);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return json;
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String json) {
        Map<String, String> map = null;
        try {
            map = mapper.readValue(json,
                    new TypeReference<HashMap<String, String>>() {});
        } catch (final IOException e) {
            log.error("JSON reading error", e);
        }
        return map;
    }
}
