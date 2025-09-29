package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallTypeConverter;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.LocalDate;

/**
 * <p>
 *      The main purpose of {@link LastfmDataSnapshot} is to group API calls that belong to the same period
 *      for a given entity_type / entity .
 * </p>
 * <p>
 *      "Snapshots" make sense when the value of an attribute may conflict with another value of the same attribute but different holder when changed.
 *      For example, two artists can share the same LastfmAttribute.RANK within a specific tag,
 *      if first artist's batch hav been processed and second has not.
 * </p>
 * <p>
 *      All the calls generated for the same <i><b>entity_type / entity</b></i> must belong to the same {@link LastfmDataSnapshot}.
 *      Then those 'snapshottable' attributes extracted for <i><b>entity / entities_relation</b></i> must refer
 *      to a specific {@link LastfmDataSnapshot} as their 'current' state.
 *      At the same time, the previous snapshot must be known in case the 'current' state is not yet finalized.
 * </p>
 */
@Entity(name = "data_snapshot")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmDataSnapshot extends BaseEntity {

    public LastfmDataSnapshot(
            @NonNull LastfmApiCallType apiCallType,
            @NonNull LocalDate dataDate) {
        this.apiCallType = apiCallType;
        this.dataDate = dataDate;
    }

    public LastfmDataSnapshot(
            @NonNull LastfmApiCallType apiCallType,
            @NonNull LocalDate dataDate,
            BaseLastfmEntity entity
    ) {
        this(apiCallType, dataDate);
        this.entityType = entity.getType();
        this.entityId = entity.getId();
    }

    @Id
    @SequenceGenerator(
            name = "data_snapshot_seq_gen",
            sequenceName = "data_snapshot_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_snapshot_seq_gen")
    private long id;

    @NonNull
    @Column(name = "api_call_type")
    @Convert(converter = ApiCallTypeConverter.class)
    private LastfmApiCallType apiCallType;

    @Column(name = "entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @NonNull
    @Column(name = "data_date")
    private LocalDate dataDate;

    @Column(name = "created_cnt")
    private int createdCount = 0;

    @Column(name = "completed_cnt")
    private int completedCount = 0;

    @Column(name = "parsed_cnt")
    private int parsedCount = 0;

}
