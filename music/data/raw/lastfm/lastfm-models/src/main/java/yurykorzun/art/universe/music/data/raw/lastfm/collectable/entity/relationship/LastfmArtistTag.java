package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;
import java.util.Objects;

@Entity(name = "artist_tag")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmArtistTag extends BaseLastfmEntityRelation<LastfmArtist, LastfmTag> {

    public static final List<String> UPDATABLE_FIELDS = List.of("usage_count");

    @Id
    @SequenceGenerator(
            name = "artist_tag_seq_gen",
            sequenceName = "artist_tag_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_tag_seq_gen")
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private LastfmArtist artist;

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
        if (!(o instanceof LastfmArtistTag that)) return false;
        
        if (this.getId() != 0 && that.getId() != 0) {
            return this.getId() == that.getId();
        }
        
        return Objects.equals(artist, that.artist) &&
               Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(artist, tag);
    }

}
