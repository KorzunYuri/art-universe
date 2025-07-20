package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "artist")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Artist extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "artist_seq_gen",
        sequenceName = "artist_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name")
    private String name;

}