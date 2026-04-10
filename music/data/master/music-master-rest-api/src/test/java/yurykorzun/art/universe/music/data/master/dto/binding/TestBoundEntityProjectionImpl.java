package yurykorzun.art.universe.music.data.master.dto.binding;

import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.data.master.model.DataSource;

@Data
@Builder
public class TestBoundEntityProjectionImpl implements BoundEntityProjection {
    private Long externalId;
    private DataSource dataSource;
    private Long masterId;
    private String masterName;
    private Long masterPrimaryArtistId;

    /** Backward-compatible constructor for existing tests (masterPrimaryArtistId defaults to null). */
    public TestBoundEntityProjectionImpl(Long externalId, DataSource dataSource, Long masterId, String masterName) {
        this.externalId = externalId;
        this.dataSource = dataSource;
        this.masterId = masterId;
        this.masterName = masterName;
    }

    /** Full constructor used by the Lombok builder. */
    public TestBoundEntityProjectionImpl(Long externalId, DataSource dataSource, Long masterId, String masterName, Long masterPrimaryArtistId) {
        this.externalId = externalId;
        this.dataSource = dataSource;
        this.masterId = masterId;
        this.masterName = masterName;
        this.masterPrimaryArtistId = masterPrimaryArtistId;
    }
}
