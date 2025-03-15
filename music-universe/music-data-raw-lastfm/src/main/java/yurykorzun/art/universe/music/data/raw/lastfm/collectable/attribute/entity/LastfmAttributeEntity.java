package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.antlr.v4.runtime.misc.NotNull;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity for synchronization of {@link LastfmAttribute} with database via
 * {@link yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeTypeSynchronizer}.
 * Must NOT be changed via application!
 */
@Entity(name = "attribute")
@Data
@EqualsAndHashCode(callSuper = false)
public class LastfmAttributeEntity extends BaseEntity {

    @Id
    private Integer id;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private Integer type;
}
