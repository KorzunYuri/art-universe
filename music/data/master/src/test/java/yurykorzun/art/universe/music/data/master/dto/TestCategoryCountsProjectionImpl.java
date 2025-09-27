package yurykorzun.art.universe.music.data.master.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestCategoryCountsProjectionImpl implements CategoryCountsProjection {
    private final Long id;
    private final String name;
    private final Integer childrenCount;
    private final Integer artistsCount;
    private final Integer tracksCount;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getChildrenCount() {
        return childrenCount;
    }

    @Override
    public Integer getArtistsCount() {
        return artistsCount;
    }

    @Override
    public Integer getTracksCount() {
        return tracksCount;
    }
}
