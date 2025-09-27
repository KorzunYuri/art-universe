package yurykorzun.art.universe.music.data.master.dto.binding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

@Data
@Builder
@AllArgsConstructor
public class TestBoundEntityProjectionImpl implements BoundEntityProjection {
    private Long externalId;
    private DataSource dataSource;
    private Long masterId;
    private String masterName;
}
