package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;
import java.util.Objects;

@Entity(name = "track_tag")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmTrackTag extends BaseLastfmEntityRelation<LastfmTrack, LastfmTag> {

    public static final List<String> UPDATABLE_FIELDS = List.of("usage_count");

    @Id
    @SequenceGenerator(
        name = "track_tag_seq_gen",
        sequenceName = "track_tag_seq",
        allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_tag_seq_gen")
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private LastfmTrack track;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private LastfmTag tag;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Override
    public List<String> getUpdatableFields() {
        return UPDATABLE_FIELDS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastfmTrackTag that)) return false;

        if (this.getId() != 0 && that.getId() != 0) {
            return this.getId() == that.getId();
        }

        return Objects.equals(track, that.track) &&
            Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(track, tag);
    }
}
