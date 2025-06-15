package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "artist_binding")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistBinding extends BaseEntity {

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
    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @NonNull
    @Column(name = "data_source_id", nullable = false)
    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;

    @NonNull
    @Column(name = "external_id", nullable = false)
    private Long externalId;

}
