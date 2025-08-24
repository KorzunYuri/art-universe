package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmSameEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityRelationTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity(name = "artist_artist")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmArtistsRelation extends BaseLastfmSameEntityRelation<LastfmArtist> {

    public static final List<String> UPDATABLE_FIELDS = List.of("match_score");

    @Id
    @SequenceGenerator(
            name = "artist_artist_seq_gen",
            sequenceName = "artist_artist_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_artist_seq_gen")
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_artist_id")
    private LastfmArtist sourceArtist;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_artist_id")
    private LastfmArtist targetArtist;

    @Column(name = "match_score", precision = 5, scale = 4)
    private BigDecimal matchScore;

    @NonNull
    @Column(name = "relation_type")
    @Convert(converter = LastfmEntityRelationTypeConverter.class)
    private LastfmEntityRelationType relationType;

    @Override
    public List<String> getUpdatableFields() {
        return UPDATABLE_FIELDS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastfmArtistsRelation that)) return false;
        
        if (this.getId() != 0 && that.getId() != 0) {
            return this.getId() == that.getId();
        }
        
        return Objects.equals(sourceArtist, that.sourceArtist) &&
               Objects.equals(targetArtist, that.targetArtist);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(sourceArtist, targetArtist);
    }

}
