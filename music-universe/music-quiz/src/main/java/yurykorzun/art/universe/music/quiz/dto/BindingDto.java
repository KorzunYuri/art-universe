package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BindingDto {
    private long masterId;
    private boolean isBound;
    private Long bindingId; // null if not bound
}
