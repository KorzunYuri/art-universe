package yurykorzun.art.universe.music.data.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistBatchLookupRequestDTO {

    @JsonProperty("names")
    @NotEmpty(message = "At least one search term is required")
    private List<String> searchTerms;
    
    private Integer limit;
}
