package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageInfoExtended {

    @JsonProperty("page")
    private int pageNumber;

    @JsonProperty("perPage")
    private int recordsPerPage;

    @JsonProperty("totalPages")
    private int pagesTotal;

    @JsonProperty("total")
    private int recordsTotal;

}
