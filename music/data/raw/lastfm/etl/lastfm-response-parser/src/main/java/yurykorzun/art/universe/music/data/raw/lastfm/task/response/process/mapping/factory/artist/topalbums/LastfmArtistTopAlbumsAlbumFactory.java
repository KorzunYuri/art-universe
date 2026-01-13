package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.topalbums;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.LastfmAlbumEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.topalbums.ArtistTopAlbumsAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

import java.util.Map;

public class LastfmArtistTopAlbumsAlbumFactory extends LastfmAlbumEntityFactory<ArtistTopAlbumsAlbumDto> {

    private final Map<String, LastfmArtist> artistsByName;

    public LastfmArtistTopAlbumsAlbumFactory(Map<String, LastfmArtist> artistsByName) {
        this.artistsByName = artistsByName;
    }

    @Override
    protected LastfmAlbum.LastfmAlbumBuilder<?, ?> setExtensionFields(LastfmAlbum.LastfmAlbumBuilder<?, ?> builder, ArtistTopAlbumsAlbumDto dto) {
        // Set play count
        builder.playCount(dto.getPlayCount());
        
        // Set artist reference from the map using artist name from DTO
        if (dto.getArtist() != null && dto.getArtist().getName() != null) {
            LastfmArtist artist = artistsByName.get(dto.getArtist().getName());
            if (artist != null) {
                builder.artist(artist);
            }
        }
        
        return builder;
    }
}
