package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity for synchronization of {@link LastfmAttribute} with database via
 * {@link yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer}.
 * Must NOT be changed via application!
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
