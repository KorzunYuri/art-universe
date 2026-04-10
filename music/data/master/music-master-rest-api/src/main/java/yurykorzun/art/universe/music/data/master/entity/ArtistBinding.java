package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.DataSourceConverter;

@Entity(name = "artist_binding")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistBinding extends MasterBaseEntity {

    @Id
    @SequenceGenerator(
        name = "artist_binding_seq_gen",
        sequenceName = "artist_binding_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_binding_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "master_id", nullable = false)
    private Long masterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", insertable = false, updatable = false)
    private Artist artist;

    @NonNull
    @Column(name = "data_source_id", nullable = false)
    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;

    @NonNull
    @Column(name = "external_id", nullable = false)
    private Long externalId;

}
