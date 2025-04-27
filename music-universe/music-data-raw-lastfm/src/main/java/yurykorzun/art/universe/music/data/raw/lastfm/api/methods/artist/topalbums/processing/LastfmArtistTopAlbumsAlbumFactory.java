package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.LastfmAlbumEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsAlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

public class LastfmArtistTopAlbumsAlbumFactory extends LastfmAlbumEntityFactory<ArtistTopAlbumsAlbumDto> {

    @Override
    protected LastfmAlbum.LastfmAlbumBuilder<?, ?> setExtensionFields(LastfmAlbum.LastfmAlbumBuilder<?, ?> builder, ArtistTopAlbumsAlbumDto dto) {
        return builder
            .playCount(dto.getPlayCount())
            ;
    }
}
