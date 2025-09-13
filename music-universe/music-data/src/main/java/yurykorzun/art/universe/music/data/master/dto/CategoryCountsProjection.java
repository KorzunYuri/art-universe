package yurykorzun.art.universe.music.data.master.dto;

public interface CategoryCountsProjection {
    Long getId();
    String getName();
    Integer getChildrenCount();
    Integer getArtistsCount();
    Integer getTracksCount();
}
