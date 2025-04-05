package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

@Entity(name = "tag")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmTag extends BaseLastfmEntity {

    @Id
    @SequenceGenerator(
            name = "tag_seq_gen",
            sequenceName = "tag_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq_gen")
    private long id;

    @Setter
    @Column(name = "usage_count")
    private int usageCount;

    @Setter
    @Column(name = "usage_users_count")
    private int usageUsersCount;

    @Override
    @Transient
    public LastfmEntityType getType() {
        return LastfmEntityType.TAG;
    }

    @Override
    public String getUniqueKey() {
        return getName();
    }
}
