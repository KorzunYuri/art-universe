package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Objects;

@Entity(name = "api_call")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmApiCall extends ApiCall {

    @Id
    @SequenceGenerator(
            name = "api_call_seq_gen",
            sequenceName = "api_call_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_call_seq_gen")
    private long id;

    @NonNull
    @Column(name = "type")
    @Convert(converter = ApiCallTypeConverter.class)
    private LastfmApiCallType type;

    @NonNull
    @Column(name = "data_snapshot_id")
    private long dataSnapshotId;

    @Column(name = "entity_type")
    private LastfmEntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LastfmApiCall apiCall)) {
            return false;
        }
        if (id != 0 && apiCall.getId() != 0) {
            return this.id == apiCall.getId();
        }
        return dataSnapshotId   == apiCall.dataSnapshotId
            && type             == apiCall.type
            && entityType       == apiCall.entityType
            && Objects.equals(entityId, apiCall.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, dataSnapshotId, entityType, entityId);
    }
}
