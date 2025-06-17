package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "track")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Track extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "track_seq_gen",
        sequenceName = "track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name", nullable = false)
    private String name;

    @NonNull
    @Column(name = "primary_artist_id", nullable = false)
    private Long primaryArtistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_artist_id", insertable = false, updatable = false)
    private Artist primaryArtist;

    @Column(name = "track_group_id")
    private Long trackGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_group_id", insertable = false, updatable = false)
    private Track originalTrack;

}
