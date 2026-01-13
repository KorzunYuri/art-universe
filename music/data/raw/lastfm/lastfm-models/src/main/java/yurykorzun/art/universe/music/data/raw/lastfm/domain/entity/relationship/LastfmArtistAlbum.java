package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity(name = "artist_album")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmArtistAlbum extends BaseLastfmEntityRelation<LastfmArtist, LastfmAlbum> {

    public static final List<String> UPDATABLE_FIELDS = Collections.emptyList();

    @Id
    @SequenceGenerator(
            name = "artist_album_seq_gen",
            sequenceName = "artist_album_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_album_seq_gen")
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private LastfmArtist artist;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private LastfmAlbum album;

    @Override
    public List<String> getUpdatableFields() {
        return UPDATABLE_FIELDS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastfmArtistAlbum that)) return false;
        
        if (this.getId() != 0 && that.getId() != 0) {
            return this.getId() == that.getId();
        }
        
        return Objects.equals(artist, that.artist) &&
               Objects.equals(album, that.album);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(artist, album);
    }

}
