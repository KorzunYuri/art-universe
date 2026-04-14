package yurykorzun.art.universe.music.data.master.category.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

@Data
@AllArgsConstructor
public class TicketSubject {

    @JsonProperty("entity_type")
    private MasterEntityType entityType;

    @JsonProperty("entity_id")
    private Long entityId;

    private String name;
}
