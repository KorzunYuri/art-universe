package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.topartists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.PageInfoExtended;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagTopArtistsPageInfo extends PageInfoExtended {

    private String tag;

}
