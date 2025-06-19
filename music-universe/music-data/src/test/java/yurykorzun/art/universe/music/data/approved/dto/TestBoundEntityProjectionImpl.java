package yurykorzun.art.universe.music.data.approved.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
public class TestBoundEntityProjectionImpl implements BoundEntityProjection {

    private final Long externalId;
    private final DataSource dataSource;
    private final Long referenceId;
    private final String referenceName;
}