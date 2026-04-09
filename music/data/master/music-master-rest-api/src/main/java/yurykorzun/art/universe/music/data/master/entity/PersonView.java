package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Read-only view of person from art_view schema.
 * Used for cross-schema validation and name resolution.
 */
@Entity
@Table(name = "v_person", schema = "art_view")
@Immutable
@Getter
@NoArgsConstructor
public class PersonView {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;
}
