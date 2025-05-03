package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.raw.lastfm.common.utils.AssertionUtils;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

@Import(LastfmAlbumServiceImpl.class)
class LastfmAlbumServiceImplTest extends JpaOnlyTest {

    @Autowired
    private LastfmAlbumServiceImpl albumService;

    @MockitoBean
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    private List<LastfmAlbum> buildAlbums(int albumsNumber) {
        return IntStream.range(0, albumsNumber)
            .mapToObj(i -> EntityCreationHelper.createAlbum(
                builder -> builder.url(String.format("url_%d", i))))
            .toList();
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenAlbums_whenFindAllByUrls_shouldCallRepository() {
        // given
        final int albumsNumber = 2;
        List<LastfmAlbum> albums = buildAlbums(albumsNumber);
        List<String> urls = albums.stream().map(LastfmAlbum::getUrl).toList();

        // when
        albumService.findAllByUrls(urls);

        // then
        AssertionUtils.verifyAndAssertInvocations(
            captor -> verify(albumRepository, times(1)).findAllByUrlIn(captor.capture()),
            List.class,
            List.of(urls),
            "albumRepository.findAllByUrlIn"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenAlbums_whenSaveAlbums_shouldCallRepository() {
        // given
        List<LastfmAlbum> albumsToSave = buildAlbums(2);

        // when
        albumService.saveAlbums(albumsToSave);

        // then
        AssertionUtils.verifyAndAssertInvocations(
            captor -> verify(albumRepository, times(1)).saveAll(captor.capture()),
            List.class,
            List.of(albumsToSave),
            "albumRepository.findAllByUrlIn"
        );
    }
}
