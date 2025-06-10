package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @Setter(value = AccessLevel.NONE)
    private long id;

    @Column(name = "name")
    private String name;

}