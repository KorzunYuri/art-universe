package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;
import java.util.Objects;

@Entity(name = "album_track")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmAlbumTrack extends BaseLastfmEntityRelation<LastfmAlbum, LastfmTrack> {

    public static final List<String> UPDATABLE_FIELDS = List.of("position");
    @Id
    @SequenceGenerator(
            name = "album_track_seq_gen",
            sequenceName = "album_track_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_track_seq_gen")
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private LastfmAlbum album;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private LastfmTrack track;

    @Column(name = "position")
    private Integer position;

    @Override
    public List<String> getUpdatableFields() {
        return UPDATABLE_FIELDS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastfmAlbumTrack that)) return false;
        
        if (this.getId() != 0 && that.getId() != 0) {
            return this.getId() == that.getId();
        }
        
        return Objects.equals(album, that.album) &&
               Objects.equals(track, that.track);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(album, track);
    }

}
