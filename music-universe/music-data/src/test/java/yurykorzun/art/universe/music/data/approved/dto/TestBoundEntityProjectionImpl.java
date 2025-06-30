package yurykorzun.art.universe.music.data.approved.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

@Data
@Builder
@AllArgsConstructor
public class TestBoundEntityProjectionImpl implements BoundEntityProjection {
    private Long externalId;
    private DataSource dataSource;
    private Long referenceId;
    private String referenceName;
}
