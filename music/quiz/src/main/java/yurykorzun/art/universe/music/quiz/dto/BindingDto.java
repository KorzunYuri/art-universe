package yurykorzun.art.universe.music.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BindingDto {

    private long masterId;

    @JsonProperty("isBound")
    private boolean isBound;

    private Long bindingId; // null if not bound
}
