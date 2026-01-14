package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

@Entity(name = "album")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Album extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "album_seq_gen",
        sequenceName = "album_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_seq_gen")
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

    @Column(name = "album_group_id")
    private Long albumGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_group_id", insertable = false, updatable = false)
    private Album originalAlbum;
}
