package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseLastfmEntityRelation<S extends BaseLastfmEntity, T extends BaseLastfmEntity> 
        extends BaseLastfmCollectable {

    /**
     * Returns list of field names that should be handled with COALESCE logic in upsert operations.
     * These fields will not be overwritten with NULL values if they already exist in the database.
     * 
     * @return List of field names for COALESCE handling
     */
    @Transient
    public abstract List<String> getUpdatableFields();

}
