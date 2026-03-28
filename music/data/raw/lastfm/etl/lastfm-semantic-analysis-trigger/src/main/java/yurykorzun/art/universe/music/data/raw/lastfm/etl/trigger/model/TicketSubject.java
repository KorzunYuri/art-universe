package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketSubject {

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_id")
    private Long entityId;

    private String name;
}
