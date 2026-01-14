package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseLastfmEntityRelationTest {

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForArtistsRelation() {
        LastfmArtistsRelation artistsRelation = EntityCreationHelper.createArtistsRelation();

        List<String> updatableFields = artistsRelation.getUpdatableFields();

        assertTrue(updatableFields.contains("match_score"));
    }

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForArtistAlbum() {
        LastfmArtistAlbum artistAlbum = EntityCreationHelper.createArtistAlbum();
        
        List<String> updatableFields = artistAlbum.getUpdatableFields();
        
        assertTrue(updatableFields.isEmpty());
    }

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForArtistTrack() {
        LastfmArtistTrack artistTrack = EntityCreationHelper.createArtistTrack();

        List<String> updatableFields = artistTrack.getUpdatableFields();

        assertTrue(updatableFields.isEmpty());
    }

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForArtistTag() {
        LastfmArtistTag artistTag = EntityCreationHelper.createArtistTag();

        List<String> updatableFields = artistTag.getUpdatableFields();

        assertTrue(updatableFields.contains("usage_count"));
    }

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForAlbumTrack() {
        LastfmAlbumTrack albumTrack = EntityCreationHelper.createAlbumTrack();

        List<String> updatableFields = albumTrack.getUpdatableFields();

        assertTrue(updatableFields.contains("position"));
    }

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForAlbumTag() {
        LastfmAlbumTag albumTag = EntityCreationHelper.createAlbumTag();

        List<String> updatableFields = albumTag.getUpdatableFields();

        assertTrue(updatableFields.contains("usage_count"));
    }

    @Test
    void getUpdatableFields_shouldReturnCorrectFieldsForTrackTag() {
        LastfmTrackTag trackTag = EntityCreationHelper.createTrackTag();

        List<String> updatableFields = trackTag.getUpdatableFields();

        assertTrue(updatableFields.contains("usage_count"));
    }
}
