package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.relation.RelationEntity;

@Entity(name = "artist_track")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistTrack extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "artist_track_seq_gen",
        sequenceName = "artist_track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_track_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    private Artist artist;

    @NonNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", insertable = false, updatable = false)
    private Track track;
    
    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ARTIST;
    }
    
    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.TRACK;
    }
    
    @Override
    public Long getFirstEntityId() {
        return artistId;
    }
    
    @Override
    public Long getSecondEntityId() {
        return trackId;
    }
}
