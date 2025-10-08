package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents paging metadata of LastFm response
 */

@Data
@NoArgsConstructor
public class PageInfo {

    private int offset;

    //  Number of items on the page
    @JsonProperty("num_res")
    private int count;

    //  Total number of items
    private int total;
}