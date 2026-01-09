package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity representing LastFM attributes in the database.
 */
@Entity(name = "attribute")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LastfmAttributeEntity extends BaseEntity {

    @Id
    private Integer id;

    @NonNull
    private String name;

    private String description;

    @NonNull
    private Integer type;
}
