package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class GameWithGenerationsDto {
    private Long id;
    private Instant createdAt;
    private List<GenerationDto> generations;
}
