package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity(name = "artist_track")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmArtistTrack extends BaseLastfmEntityRelation<LastfmArtist, LastfmTrack> {

    public static final List<String> UPDATABLE_FIELDS = Collections.emptyList();
    @Id
    @SequenceGenerator(
            name = "artist_track_seq_gen",
            sequenceName = "artist_track_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_track_seq_gen")
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private LastfmArtist artist;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private LastfmTrack track;

    @Override
    public List<String> getUpdatableFields() {
        return UPDATABLE_FIELDS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastfmArtistTrack that)) return false;
        
        if (this.getId() != 0 && that.getId() != 0) {
            return this.getId() == that.getId();
        }
        
        return Objects.equals(artist, that.artist) &&
               Objects.equals(track, that.track);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(artist, track);
    }

}
